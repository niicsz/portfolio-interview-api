package com.binitech.interview.application.ports.inbound;

public interface IndexKnowledgeUseCasePort {

  /** Reindexa toda a base de conhecimento e devolve a quantidade de trechos indexados. */
  int reindex();
}
