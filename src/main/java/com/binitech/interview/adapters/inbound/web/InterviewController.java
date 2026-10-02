package com.binitech.interview.adapters.inbound.web;

import com.binitech.interview.adapters.inbound.web.generated.api.InterviewApi;
import com.binitech.interview.adapters.inbound.web.generated.model.AnswerResponseDTO;
import com.binitech.interview.adapters.inbound.web.generated.model.QuestionRequestDTO;
import com.binitech.interview.application.ports.inbound.AskQuestionUseCasePort;
import com.binitech.interview.domain.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InterviewController implements InterviewApi {

  private static final Logger log = LoggerFactory.getLogger(InterviewController.class);

  private final AskQuestionUseCasePort askQuestionUseCase;
  private final WebMapper webMapper;

  public InterviewController(AskQuestionUseCasePort askQuestionUseCase, WebMapper webMapper) {
    this.askQuestionUseCase = askQuestionUseCase;
    this.webMapper = webMapper;
  }

  @Override
  public ResponseEntity<AnswerResponseDTO> askQuestion(QuestionRequestDTO questionRequestDTO) {

    log.debug("Pergunta recebida com {} caracteres", questionRequestDTO.getQuestion().length());
    Answer answer = askQuestionUseCase.ask(questionRequestDTO.getQuestion());
    log.info("Pergunta processada: status={}", answer.status());
    return ResponseEntity.ok(webMapper.toDto(answer));
  }
}
