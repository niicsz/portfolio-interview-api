package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.GeneratedAnswer;
import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import java.util.List;

public interface AnswerGeneratorPort {

  GeneratedAnswer generate(Question question, List<KnowledgeChunk> context);
}
