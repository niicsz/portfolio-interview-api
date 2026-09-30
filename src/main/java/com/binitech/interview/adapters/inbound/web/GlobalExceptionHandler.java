package com.binitech.interview.adapters.inbound.web;

import com.binitech.interview.adapters.inbound.web.generated.model.ErrorDTO;
import com.binitech.interview.domain.exception.InvalidQuestionException;
import com.binitech.interview.domain.exception.LanguageModelUnavailableException;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String INVALID_QUESTION = "A pergunta deve ter entre 3 e 500 caracteres.";

  @ExceptionHandler(InvalidQuestionException.class)
  public ResponseEntity<ErrorDTO> handleInvalidQuestion(InvalidQuestionException e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException e) {
    return error(HttpStatus.BAD_REQUEST, INVALID_QUESTION);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorDTO> handleUnreadable(HttpMessageNotReadableException e) {
    return error(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorDTO> handleMediaType(HttpMediaTypeNotSupportedException e) {
    return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Use Content-Type: application/json.");
  }

  @ExceptionHandler(LanguageModelUnavailableException.class)
  public ResponseEntity<ErrorDTO> handleModelUnavailable(LanguageModelUnavailableException e) {
    return error(
        HttpStatus.SERVICE_UNAVAILABLE,
        "O assistente está indisponível no momento. Tente novamente em instantes.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDTO> handleUnexpected(Exception e) {
    log.error("Erro inesperado", e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno.");
  }

  private ResponseEntity<ErrorDTO> error(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorDTO(status.value(), message, OffsetDateTime.now()));
  }
}
