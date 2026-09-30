package com.binitech.interview.application.ports.outbound;

import com.binitech.interview.domain.GeneratedAnswer;
import com.binitech.interview.domain.Question;
import com.binitech.interview.domain.knowledge.KnowledgeChunk;
import java.util.List;

public interface AnswerGeneratorPort {

  /**
   * Gera uma resposta usando apenas {@code context}.
   *
   * @throws com.binitech.interview.domain.exception.UnsafeModelOutputException se a saída vazar
   *     instruções internas
   */
  GeneratedAnswer generate(Question question, List<KnowledgeChunk> context);
}
