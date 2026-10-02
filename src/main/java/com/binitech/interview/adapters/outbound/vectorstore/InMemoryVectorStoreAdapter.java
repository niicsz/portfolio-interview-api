package com.binitech.interview.adapters.outbound.vectorstore;

import com.binitech.interview.application.ports.outbound.VectorStorePort;
import com.binitech.interview.domain.knowledge.Embedding;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import com.binitech.interview.domain.knowledge.RetrievedChunk;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryVectorStoreAdapter implements VectorStorePort {

  private record Entry(KnowledgeChunk chunk, Embedding embedding) {}

  private volatile List<Entry> entries = List.of();

  @Override
  public void replaceAll(Map<KnowledgeChunk, Embedding> chunks) {
    entries =
        chunks.entrySet().stream()
            .map(entry -> new Entry(entry.getKey(), entry.getValue()))
            .toList();
  }

  @Override
  public List<RetrievedChunk> search(Embedding query, int topK) {
    return entries.stream()
        .map(entry -> new RetrievedChunk(entry.chunk(), query.cosineSimilarity(entry.embedding())))
        .sorted(Comparator.comparingDouble(RetrievedChunk::score).reversed())
        .limit(topK)
        .toList();
  }
}
