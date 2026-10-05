package com.binitech.interview.adapters.inbound.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.binitech.interview.application.ports.inbound.AskQuestionUseCasePort;
import com.binitech.interview.application.ports.inbound.IndexKnowledgeUseCasePort;
import com.binitech.interview.application.ports.outbound.EmbeddingPort;
import com.binitech.interview.domain.Answer;
import com.binitech.interview.domain.exception.LanguageModelUnavailableException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InterviewApiIntegrationTest {

  private static final String ORIGIN = "https://portfolio.example.com";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AskQuestionUseCasePort askQuestionUseCase;
  @MockitoBean private IndexKnowledgeUseCasePort indexKnowledgeUseCase;
  @MockitoBean private EmbeddingPort embeddingPort;

  @Test
  void answersQuestion() throws Exception {
    when(askQuestionUseCase.ask(anyString()))
        .thenReturn(Answer.answered("Ele trabalha no Bradesco.", List.of("Cargo e período")));

    mockMvc
        .perform(ask("{\"question\":\"Onde ele trabalha?\"}", "203.0.113.1"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
        .andExpect(jsonPath("$.status").value("ANSWERED"))
        .andExpect(jsonPath("$.answer").value("Ele trabalha no Bradesco."))
        .andExpect(jsonPath("$.sources[0]").value("Cargo e período"));
  }

  @Test
  void rejectsTooLongQuestionBeforeTheUseCase() throws Exception {
    String question = "a".repeat(501);

    mockMvc
        .perform(ask("{\"question\":\"" + question + "\"}", "203.0.113.2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    verify(askQuestionUseCase, never()).ask(anyString());
  }

  @Test
  void rejectsMissingQuestion() throws Exception {
    mockMvc.perform(ask("{}", "203.0.113.3")).andExpect(status().isBadRequest());
  }

  @Test
  void mapsModelOutageTo503WithoutLeakingDetails() throws Exception {
    when(askQuestionUseCase.ask(anyString()))
        .thenThrow(new LanguageModelUnavailableException("detalhe interno", null));

    mockMvc
        .perform(ask("{\"question\":\"Onde ele trabalha?\"}", "203.0.113.4"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(
            jsonPath("$.message")
                .value("O assistente está indisponível no momento. Tente novamente em instantes."));
  }

  @Test
  void rateLimitsPerClientAndKeepsCorsHeadersOn429() throws Exception {
    when(askQuestionUseCase.ask(anyString())).thenReturn(Answer.outOfScope());
    String body = "{\"question\":\"Onde ele trabalha?\"}";

    mockMvc.perform(ask(body, "203.0.113.5")).andExpect(status().isOk());
    mockMvc.perform(ask(body, "203.0.113.5")).andExpect(status().isOk());
    mockMvc
        .perform(ask(body, "203.0.113.5"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));

    mockMvc.perform(ask(body, "203.0.113.6")).andExpect(status().isOk());
  }

  @Test
  void spoofedForwardedForDoesNotBypassRateLimit() throws Exception {
    when(askQuestionUseCase.ask(anyString())).thenReturn(Answer.outOfScope());
    String body = "{\"question\":\"Onde ele trabalha?\"}";

    for (int i = 0; i < 2; i++) {
      mockMvc.perform(ask(body, "10.0.0." + i + ", 203.0.113.7")).andExpect(status().isOk());
    }
    mockMvc.perform(ask(body, "10.0.0.99, 203.0.113.7")).andExpect(status().isTooManyRequests());
  }

  @Test
  void blocksUnknownOrigins() throws Exception {
    mockMvc
        .perform(
            options("/api/interview/questions")
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }

  @Test
  void mapsUnknownPathTo404() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void mapsUnsupportedMethodTo405() throws Exception {
    mockMvc
        .perform(get("/api/interview/questions"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405));
  }

  private MockHttpServletRequestBuilder ask(String body, String forwardedFor) {
    return post("/api/interview/questions")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Origin", ORIGIN)
        .header("X-Forwarded-For", forwardedFor)
        .content(body);
  }
}
