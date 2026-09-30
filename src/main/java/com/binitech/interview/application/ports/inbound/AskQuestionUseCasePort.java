package com.binitech.interview.application.ports.inbound;

import com.binitech.interview.domain.Answer;

public interface AskQuestionUseCasePort {

  Answer ask(String rawQuestion);
}
