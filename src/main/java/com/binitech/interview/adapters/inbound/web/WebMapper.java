package com.binitech.interview.adapters.inbound.web;

import com.binitech.interview.adapters.inbound.web.generated.model.AnswerResponseDTO;
import com.binitech.interview.adapters.inbound.web.generated.model.AnswerStatusDTO;
import com.binitech.interview.domain.Answer;
import org.springframework.stereotype.Component;

@Component
public class WebMapper {

  public AnswerResponseDTO toDto(Answer answer) {
    return new AnswerResponseDTO(
        AnswerStatusDTO.valueOf(answer.status().name()), answer.text(), answer.sources());
  }
}
