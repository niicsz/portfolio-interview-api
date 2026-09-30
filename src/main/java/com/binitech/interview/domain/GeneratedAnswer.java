package com.binitech.interview.domain;

/** Rascunho devolvido pelo modelo antes de passar pelas verificações de saída. */
public record GeneratedAnswer(boolean answerable, String text) {

  public static GeneratedAnswer notAnswerable() {
    return new GeneratedAnswer(false, "");
  }
}
