package com.binitech.interview.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "interview")
public record InterviewProperties(
    @NotEmpty List<String> allowedOrigins,
    @Valid @NotNull Retrieval retrieval,
    @Min(100) int maxAnswerChars,
    @Valid @NotNull EmbeddingModel embedding,
    @Valid @NotNull Llm llm,
    @Valid @NotNull RateLimit rateLimit) {

  public record Retrieval(
      @Min(1) int topK,
      @DecimalMin("0.0") @DecimalMax("1.0") double minRelevance,
      @Min(200) int maxChunkChars) {}

  public record EmbeddingModel(@NotNull Path modelPath, @NotNull Path tokenizerPath) {}

  public record Llm(
      @NotBlank String apiKey,
      @NotBlank String model,
      @NotBlank String classifierModel,
      @NotNull Duration timeout,
      @Min(0) int maxRetries,
      String effort) {}

  public record RateLimit(
      @Min(1) int perClientPerMinute,
      @Min(1) int perClientPerDay,
      @Min(1) int globalPerDay,
      @Min(0) int trustedProxyHops) {}
}
