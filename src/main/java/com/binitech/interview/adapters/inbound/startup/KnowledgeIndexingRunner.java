package com.binitech.interview.adapters.inbound.startup;

import com.binitech.interview.application.ports.inbound.IndexKnowledgeUseCasePort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Indexa a base de conhecimento na subida. O readiness só vira ACCEPTING_TRAFFIC depois dos
 * ApplicationRunners, então o health check de readiness não libera tráfego com a base vazia.
 */
@Component
public class KnowledgeIndexingRunner implements ApplicationRunner {

  private final IndexKnowledgeUseCasePort indexKnowledgeUseCase;

  public KnowledgeIndexingRunner(IndexKnowledgeUseCasePort indexKnowledgeUseCase) {
    this.indexKnowledgeUseCase = indexKnowledgeUseCase;
  }

  @Override
  public void run(ApplicationArguments args) {
    indexKnowledgeUseCase.reindex();
  }
}
