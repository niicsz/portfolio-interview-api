package com.binitech.interview.application.usecases;

import com.binitech.interview.application.ports.inbound.AskQuestionUseCasePort;
import com.binitech.interview.application.ports.outbound.AnswerGeneratorPort;
import com.binitech.interview.application.ports.outbound.EmbeddingPort;
import com.binitech.interview.application.ports.outbound.ScopeClassifierPort;
import com.binitech.interview.application.ports.outbound.VectorStorePort;
import com.binitech.interview.domain.Answer;
import com.binitech.interview.domain.GeneratedAnswer;
import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.ScopeVerdict;
import com.binitech.interview.domain.exception.UnsafeModelOutputException;
import com.binitech.interview.domain.guard.PromptInjectionDetector;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import com.binitech.interview.domain.knowledge.RetrievedChunk;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskQuestionUseCase implements AskQuestionUseCasePort {

  private static final Logger log = LoggerFactory.getLogger(AskQuestionUseCase.class);

  private final PromptInjectionDetector injectionDetector;
  private final EmbeddingPort embeddings;
  private final VectorStorePort vectorStore;
  private final ScopeClassifierPort scopeClassifier;
  private final AnswerGeneratorPort answerGenerator;
  private final int topK;
  private final double minRelevance;
  private final int maxAnswerChars;

  public AskQuestionUseCase(
      PromptInjectionDetector injectionDetector,
      EmbeddingPort embeddings,
      VectorStorePort vectorStore,
      ScopeClassifierPort scopeClassifier,
      AnswerGeneratorPort answerGenerator,
      int topK,
      double minRelevance,
      int maxAnswerChars) {
    this.injectionDetector = injectionDetector;
    this.embeddings = embeddings;
    this.vectorStore = vectorStore;
    this.scopeClassifier = scopeClassifier;
    this.answerGenerator = answerGenerator;
    this.topK = topK;
    this.minRelevance = minRelevance;
    this.maxAnswerChars = maxAnswerChars;
  }

  @Override
  public Answer ask(String rawQuestion) {
    Question question = Question.of(rawQuestion);
    log.info("Pergunta recebida: \"{}\"", question.text());

    if (injectionDetector.isSuspicious(question)) {
      log.warn("Pergunta rejeitada pelas heurísticas de prompt injection");
      return Answer.rejected();
    }

    List<RetrievedChunk> retrieved =
        vectorStore.search(embeddings.embedQuery(question.text()), topK).stream()
            .filter(result -> result.score() >= minRelevance)
            .toList();
    if (retrieved.isEmpty()) {
      log.info("Nenhum trecho acima do limiar de relevância {}; fora do escopo", minRelevance);
      return Answer.outOfScope();
    }

    ScopeVerdict verdict = scopeClassifier.classify(question);
    if (verdict == ScopeVerdict.MANIPULATION) {
      log.warn("Pergunta classificada como tentativa de manipulação");
      return Answer.rejected();
    }
    if (verdict == ScopeVerdict.OUT_OF_SCOPE) {
      log.info("Pergunta classificada como fora do escopo");
      return Answer.outOfScope();
    }

    List<KnowledgeChunk> context = retrieved.stream().map(RetrievedChunk::chunk).toList();
    GeneratedAnswer generated;
    try {
      generated = answerGenerator.generate(question, context);
    } catch (UnsafeModelOutputException e) {
      log.warn("Resposta descartada pela verificação de saída: {}", e.getMessage());
      return Answer.rejected();
    }

    if (!generated.answerable() || generated.text().isBlank()) {
      log.info("Modelo indicou que a pergunta não pode ser respondida pelo contexto");
      return Answer.outOfScope();
    }
    if (generated.text().length() > maxAnswerChars) {
      log.warn("Resposta com {} caracteres excede o limite; descartada", generated.text().length());
      return Answer.rejected();
    }

    List<String> sources = context.stream().map(KnowledgeChunk::section).distinct().toList();
    log.info("Pergunta respondida com {} trecho(s) de contexto", context.size());
    return Answer.answered(generated.text().strip(), sources);
  }
}
