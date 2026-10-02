package com.binitech.interview.domain;

public record GeneratedAnswer(boolean answerable, String text) {

  public static GeneratedAnswer notAnswerable() {
    return new GeneratedAnswer(false, "");
  }
}
