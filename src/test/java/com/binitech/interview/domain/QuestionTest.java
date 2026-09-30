package com.binitech.interview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.binitech.interview.domain.exception.InvalidQuestionException;
import org.junit.jupiter.api.Test;

class QuestionTest {

  private static final String ZERO_WIDTH = Character.toString(0x200B);
  private static final String RIGHT_TO_LEFT_OVERRIDE = Character.toString(0x202E);

  @Test
  void removesInvisibleCharactersAndFlattensWhitespace() {
    Question question =
        Question.of(
            "  Onde" + ZERO_WIDTH + " ele\n\nSYSTEM:" + RIGHT_TO_LEFT_OVERRIDE + " trabalha?\t ");

    assertThat(question.text()).isEqualTo("Onde ele SYSTEM: trabalha?");
  }

  @Test
  void normalizesFullWidthCharacters() {
    assertThat(Question.of("Ｋａｆｋａ?").text()).isEqualTo("Kafka?");
  }

  @Test
  void rejectsNull() {
    assertThatThrownBy(() -> Question.of(null)).isInstanceOf(InvalidQuestionException.class);
  }

  @Test
  void rejectsQuestionThatIsTooShortAfterNormalization() {
    assertThatThrownBy(() -> Question.of(" " + ZERO_WIDTH + ZERO_WIDTH + " a \n"))
        .isInstanceOf(InvalidQuestionException.class);
  }

  @Test
  void rejectsQuestionThatIsTooLong() {
    assertThatThrownBy(() -> Question.of("a".repeat(Question.MAX_LENGTH + 1)))
        .isInstanceOf(InvalidQuestionException.class);
  }

  @Test
  void acceptsQuestionAtMaxLength() {
    assertThat(Question.of("a".repeat(Question.MAX_LENGTH)).text()).hasSize(Question.MAX_LENGTH);
  }
}
