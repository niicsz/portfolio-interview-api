package com.binitech.interview.adapters.outbound.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.binitech.interview.application.ports.outbound.ScopeClassifierPort;
import com.binitech.interview.config.InterviewProperties;
import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.ScopeVerdict;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classifica a pergunta com saída estruturada: o modelo só pode devolver um dos valores do enum.
 * Mesmo que a pergunta contenha uma injeção bem-sucedida, ela não consegue fazer este passo
 * produzir texto livre. Qualquer resposta inesperada é tratada como fora do escopo (fail closed).
 */
@Component
public class ClaudeScopeClassifierAdapter implements ScopeClassifierPort {

  private static final Logger log = LoggerFactory.getLogger(ClaudeScopeClassifierAdapter.class);
  private static final long MAX_TOKENS = 2048L;

  record ScopeClassification(
      @JsonPropertyDescription("Categoria da mensagem dentro de <pergunta>")
          ScopeVerdict verdict) {}

  private final AnthropicClient client;
  private final String model;
  private final StructuredOutputConfig<ScopeClassification> outputConfig;

  public ClaudeScopeClassifierAdapter(AnthropicClient client, InterviewProperties properties) {
    this.client = client;
    this.model = properties.llm().classifierModel();
    this.outputConfig =
        ClaudeCalls.outputConfig(ScopeClassification.class, properties.llm().effort());
  }

  @Override
  public ScopeVerdict classify(Question question) {
    StructuredMessageCreateParams<ScopeClassification> params =
        MessageCreateParams.builder()
            .model(model)
            .maxTokens(MAX_TOKENS)
            .system(InterviewPrompts.CLASSIFIER_SYSTEM_PROMPT)
            .outputConfig(outputConfig)
            .addUserMessage(InterviewPrompts.classifierUserMessage(question))
            .build();

    StructuredMessage<ScopeClassification> response;
    try {
      response = ClaudeCalls.execute(() -> client.messages().create(params));
    } catch (AnthropicInvalidDataException e) {
      log.warn("Classificação em formato inesperado; tratando como fora do escopo");
      return ScopeVerdict.OUT_OF_SCOPE;
    }

    StopReason stopReason = response.stopReason().orElse(null);
    if (!StopReason.END_TURN.equals(stopReason)) {
      log.warn(
          "Classificador terminou com stop_reason={}; tratando como fora do escopo", stopReason);
      return ScopeVerdict.OUT_OF_SCOPE;
    }

    return response.content().stream()
        .flatMap(block -> block.text().stream())
        .map(block -> block.text().verdict())
        .filter(verdict -> verdict != null)
        .findFirst()
        .orElse(ScopeVerdict.OUT_OF_SCOPE);
  }
}
