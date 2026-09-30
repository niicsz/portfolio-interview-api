package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.knowledge.Embedding;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import com.binitech.interview.domain.knowledge.RetrievedChunk;
import java.util.List;
import java.util.Map;

public interface VectorStorePort {

  void replaceAll(Map<KnowledgeChunk, Embedding> chunks);

  /** Trechos mais similares, em ordem decrescente de score. */
  List<RetrievedChunk> search(Embedding query, int topK);
}
