package com.binitech.interview.application.usecases;

import com.binitech.interview.application.ports.inbound.IndexKnowledgeUseCasePort;
import com.binitech.interview.application.ports.outbound.EmbeddingPort;
import com.binitech.interview.application.ports.outbound.KnowledgeSourcePort;
import com.binitech.interview.application.ports.outbound.VectorStorePort;
import com.binitech.interview.domain.knowledge.Embedding;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import com.binitech.interview.domain.knowledge.KnowledgeChunker;
import com.binitech.interview.domain.knowledge.KnowledgeDocument;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexKnowledgeUseCase implements IndexKnowledgeUseCasePort {

  private static final Logger log = LoggerFactory.getLogger(IndexKnowledgeUseCase.class);

  private final KnowledgeSourcePort knowledgeSource;
  private final KnowledgeChunker chunker;
  private final EmbeddingPort embeddings;
  private final VectorStorePort vectorStore;

  public IndexKnowledgeUseCase(
      KnowledgeSourcePort knowledgeSource,
      KnowledgeChunker chunker,
      EmbeddingPort embeddings,
      VectorStorePort vectorStore) {
    this.knowledgeSource = knowledgeSource;
    this.chunker = chunker;
    this.embeddings = embeddings;
    this.vectorStore = vectorStore;
  }

  @Override
  public int reindex() {
    List<KnowledgeDocument> documents = knowledgeSource.loadAll();
    if (documents.isEmpty()) {
      throw new IllegalStateException("A base de conhecimento está vazia");
    }

    Map<KnowledgeChunk, Embedding> indexed = new LinkedHashMap<>();
    for (KnowledgeDocument document : documents) {
      for (KnowledgeChunk chunk : chunker.chunk(document)) {
        indexed.put(chunk, embeddings.embedPassage(chunk.content()));
      }
    }
    vectorStore.replaceAll(indexed);
    log.info(
        "Base de conhecimento indexada: {} documento(s), {} trecho(s)",
        documents.size(),
        indexed.size());
    return indexed.size();
  }
}
