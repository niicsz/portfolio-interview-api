package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.knowledge.Embedding;

public interface EmbeddingPort {

  /** Embedding de um trecho da base de conhecimento. */
  Embedding embedPassage(String text);

  /** Embedding de uma pergunta (alguns modelos tratam consulta e passagem de forma distinta). */
  Embedding embedQuery(String text);
}
