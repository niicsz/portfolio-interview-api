package com.binitech.interview.adapters.outbound.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.binitech.interview.application.ports.outbound.AnswerGeneratorPort;
import com.binitech.interview.config.InterviewProperties;
import com.binitech.interview.domain.GeneratedAnswer;
import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.exception.UnsafeModelOutputException;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gera a resposta a partir do contexto recuperado. O system prompt carrega um código canário
 * aleatório (novo a cada subida); se ele ou um trecho do prompt aparecer na resposta, a resposta é
 * descartada.
 */
@Component
public class ClaudeAnswerGeneratorAdapter implements AnswerGeneratorPort {

  private static final Logger log = LoggerFactory.getLogger(ClaudeAnswerGeneratorAdapter.class);
  private static final long MAX_TOKENS = 4096L;

  record AnswerDraft(
      @JsonPropertyDescription(
              "true somente se a pergunta for sobre a vida profissional do Nicolas e o contexto"
                  + " responder a ela")
          boolean answerable,
      @JsonPropertyDescription("Resposta em texto simples, ou vazio quando answerable for false")
          String answer) {}

  private final AnthropicClient client;
  private final String model;
  private final StructuredOutputConfig<AnswerDraft> outputConfig;
  private final String systemPrompt;
  private final PromptLeakDetector leakDetector;

  public ClaudeAnswerGeneratorAdapter(AnthropicClient client, InterviewProperties properties) {
    this.client = client;
    this.model = properties.llm().model();
    this.outputConfig = ClaudeCalls.outputConfig(AnswerDraft.class, properties.llm().effort());
    byte[] random = new byte[12];
    new SecureRandom().nextBytes(random);
    String canary = "CNR-" + HexFormat.of().formatHex(random);
    this.systemPrompt = InterviewPrompts.answerSystemPrompt(canary);
    this.leakDetector = new PromptLeakDetector(systemPrompt, canary);
  }

  @Override
  public GeneratedAnswer generate(Question question, List<KnowledgeChunk> context) {
    StructuredMessageCreateParams<AnswerDraft> params =
        MessageCreateParams.builder()
            .model(model)
            .maxTokens(MAX_TOKENS)
            .system(systemPrompt)
            .outputConfig(outputConfig)
            .addUserMessage(InterviewPrompts.answerUserMessage(question, context))
            .build();

    StructuredMessage<AnswerDraft> response;
    try {
      response = ClaudeCalls.execute(() -> client.messages().create(params));
    } catch (AnthropicInvalidDataException e) {
      throw new UnsafeModelOutputException("resposta fora do schema esperado");
    }

    StopReason stopReason = response.stopReason().orElse(null);
    if (!StopReason.END_TURN.equals(stopReason)) {
      log.warn("Gerador terminou com stop_reason={}; resposta descartada", stopReason);
      return GeneratedAnswer.notAnswerable();
    }

    AnswerDraft draft =
        response.content().stream()
            .flatMap(block -> block.text().stream())
            .map(block -> block.text())
            .findFirst()
            .orElseThrow(() -> new UnsafeModelOutputException("resposta sem conteúdo"));

    String text = draft.answer() == null ? "" : draft.answer();
    if (leakDetector.leaks(text)) {
      throw new UnsafeModelOutputException("resposta contém trechos do system prompt");
    }
    return new GeneratedAnswer(draft.answerable(), text);
  }
}
