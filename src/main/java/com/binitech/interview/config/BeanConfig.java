package com.binitech.interview.config;

import com.binitech.interview.application.ports.inbound.AskQuestionUseCasePort;
import com.binitech.interview.application.ports.inbound.IndexKnowledgeUseCasePort;
import com.binitech.interview.application.ports.outbound.AnswerGeneratorPort;
import com.binitech.interview.application.ports.outbound.EmbeddingPort;
import com.binitech.interview.application.ports.outbound.KnowledgeSourcePort;
import com.binitech.interview.application.ports.outbound.ScopeClassifierPort;
import com.binitech.interview.application.ports.outbound.VectorStorePort;
import com.binitech.interview.application.usecases.AskQuestionUseCase;
import com.binitech.interview.application.usecases.IndexKnowledgeUseCase;
import com.binitech.interview.domain.guard.PromptInjectionDetector;
import com.binitech.interview.domain.knowledge.KnowledgeChunker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  private static final Logger log = LoggerFactory.getLogger(BeanConfig.class);

  @Bean
  public IndexKnowledgeUseCasePort indexKnowledgeUseCasePort(
      KnowledgeSourcePort knowledgeSource,
      EmbeddingPort embeddings,
      VectorStorePort vectorStore,
      InterviewProperties properties) {
    log.info("Configurando IndexKnowledgeUseCasePort");
    return new IndexKnowledgeUseCase(
        knowledgeSource,
        new KnowledgeChunker(properties.retrieval().maxChunkChars()),
        embeddings,
        vectorStore);
  }

  @Bean
  public AskQuestionUseCasePort askQuestionUseCasePort(
      EmbeddingPort embeddings,
      VectorStorePort vectorStore,
      ScopeClassifierPort scopeClassifier,
      AnswerGeneratorPort answerGenerator,
      InterviewProperties properties) {
    log.info(
        "Configurando AskQuestionUseCasePort (modelo={}, classificador={})",
        properties.llm().model(),
        properties.llm().classifierModel());
    return new AskQuestionUseCase(
        new PromptInjectionDetector(),
        embeddings,
        vectorStore,
        scopeClassifier,
        answerGenerator,
        properties.retrieval().topK(),
        properties.retrieval().minRelevance(),
        properties.maxAnswerChars());
  }
}
