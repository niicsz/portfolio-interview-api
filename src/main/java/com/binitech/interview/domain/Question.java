package com.binitech.interview.domain;

import com.binitech.interview.domain.exception.InvalidQuestionException;
import java.text.Normalizer;
import java.util.regex.Pattern;

public final class Question {

  public static final int MIN_LENGTH = 3;
  public static final int MAX_LENGTH = 500;

  private static final Pattern INVISIBLE_OR_CONTROL = Pattern.compile("[\\p{C}&&[^\\s]]");
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private final String text;

  private Question(String text) {
    this.text = text;
  }

  public static Question of(String raw) {
    if (raw == null) {
      throw new InvalidQuestionException("A pergunta é obrigatória.");
    }
    String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);
    normalized = INVISIBLE_OR_CONTROL.matcher(normalized).replaceAll("");
    normalized = WHITESPACE.matcher(normalized).replaceAll(" ").strip();

    int length = normalized.codePointCount(0, normalized.length());
    if (length < MIN_LENGTH) {
      throw new InvalidQuestionException(
          "A pergunta deve ter pelo menos " + MIN_LENGTH + " caracteres.");
    }
    if (length > MAX_LENGTH) {
      throw new InvalidQuestionException(
          "A pergunta deve ter no máximo " + MAX_LENGTH + " caracteres.");
    }
    return new Question(normalized);
  }

  public String text() {
    return text;
  }

  @Override
  public String toString() {
    return text;
  }
}
