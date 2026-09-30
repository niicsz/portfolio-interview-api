package com.binitech.interview.domain.knowledge;

public record Embedding(float[] vector) {

  public double cosineSimilarity(Embedding other) {
    if (vector.length != other.vector.length) {
      throw new IllegalArgumentException("Embeddings com dimensões diferentes");
    }
    double dot = 0;
    double normA = 0;
    double normB = 0;
    for (int i = 0; i < vector.length; i++) {
      dot += vector[i] * other.vector[i];
      normA += vector[i] * vector[i];
      normB += other.vector[i] * other.vector[i];
    }
    if (normA == 0 || normB == 0) {
      return 0;
    }
    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
  }
}
