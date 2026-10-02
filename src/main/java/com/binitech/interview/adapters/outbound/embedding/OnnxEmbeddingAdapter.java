package com.binitech.interview.adapters.outbound.embedding;

import com.binitech.interview.application.ports.outbound.EmbeddingPort;
import com.binitech.interview.config.InterviewProperties;
import com.binitech.interview.domain.knowledge.Embedding;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OnnxEmbeddingAdapter implements EmbeddingPort {

  private static final Logger log = LoggerFactory.getLogger(OnnxEmbeddingAdapter.class);

  private final OnnxEmbeddingModel model;

  public OnnxEmbeddingAdapter(InterviewProperties properties) {
    Path modelPath = properties.embedding().modelPath();
    Path tokenizerPath = properties.embedding().tokenizerPath();
    if (!Files.isRegularFile(modelPath) || !Files.isRegularFile(tokenizerPath)) {
      throw new IllegalStateException(
          "Modelo de embeddings não encontrado em "
              + modelPath
              + " / "
              + tokenizerPath
              + ". Rode scripts/download-embedding-model.sh.");
    }
    log.info("Carregando modelo de embeddings de {}", modelPath);
    this.model = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
  }

  @Override
  public Embedding embedPassage(String text) {
    return embed("passage: " + text);
  }

  @Override
  public Embedding embedQuery(String text) {
    return embed("query: " + text);
  }

  private Embedding embed(String text) {
    return new Embedding(model.embed(text).content().vector());
  }
}
