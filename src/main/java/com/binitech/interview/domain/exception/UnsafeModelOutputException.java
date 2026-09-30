package com.binitech.interview.domain.exception;

/** A resposta do modelo vazou instruções internas ou saiu do formato esperado. */
public class UnsafeModelOutputException extends BusinessException {

  public UnsafeModelOutputException(String message) {
    super(message);
  }
}
