package com.binitech.interview.domain;

import java.util.List;

public record Answer(AnswerStatus status, String text, List<String> sources) {

  static final String OUT_OF_SCOPE_MESSAGE =
      "Só consigo responder perguntas sobre a carreira do Nicolas: experiências, projetos,"
          + " formação e tecnologias. / I can only answer questions about Nicolas's career:"
          + " experience, projects, education and tech stack.";

  static final String REJECTED_MESSAGE =
      "Não posso ajudar com esse pedido. Que tal perguntar sobre as experiências ou projetos do"
          + " Nicolas? / I can't help with that request. Try asking about Nicolas's experience or"
          + " projects.";

  public Answer {
    sources = List.copyOf(sources);
  }

  public static Answer answered(String text, List<String> sources) {
    return new Answer(AnswerStatus.ANSWERED, text, sources);
  }

  public static Answer outOfScope() {
    return new Answer(AnswerStatus.OUT_OF_SCOPE, OUT_OF_SCOPE_MESSAGE, List.of());
  }

  public static Answer rejected() {
    return new Answer(AnswerStatus.REJECTED, REJECTED_MESSAGE, List.of());
  }
}
