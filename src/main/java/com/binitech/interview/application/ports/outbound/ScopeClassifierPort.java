package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.ScopeVerdict;

public interface ScopeClassifierPort {

  ScopeVerdict classify(Question question);
}
