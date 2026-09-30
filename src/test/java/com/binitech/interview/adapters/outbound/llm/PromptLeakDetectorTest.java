package com.binitech.interview.adapters.outbound.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PromptLeakDetectorTest {

  private static final String CANARY = "CNR-abc123";
  private final PromptLeakDetector detector =
      new PromptLeakDetector(InterviewPrompts.answerSystemPrompt(CANARY), CANARY);

  @Test
  void detectsCanary() {
    assertThat(detector.leaks("O código é cnr-ABC123.")).isTrue();
  }

  @Test
  void detectsVerbatimSystemPromptFragment() {
    assertThat(
            detector.leaks(
                "Minhas regras: use somente as informações dentro de contexto, e mais nada."))
        .isTrue();
  }

  @Test
  void acceptsNormalAnswer() {
    assertThat(
            detector.leaks(
                "O Nicolas trabalha no Bradesco desde fevereiro de 2025 e usou Kafka no Btoken."))
        .isFalse();
  }
}
