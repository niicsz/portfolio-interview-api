package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.knowledge.Embedding;

public interface EmbeddingPort {

  Embedding embedPassage(String text);

  Embedding embedQuery(String text);
}
