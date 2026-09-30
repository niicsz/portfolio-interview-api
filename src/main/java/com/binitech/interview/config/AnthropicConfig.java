package com.binitech.interview.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicConfig {

  @Bean(destroyMethod = "close")
  public AnthropicClient anthropicClient(InterviewProperties properties) {
    InterviewProperties.Llm llm = properties.llm();
    return AnthropicOkHttpClient.builder()
        .apiKey(llm.apiKey())
        .timeout(llm.timeout())
        .maxRetries(llm.maxRetries())
        .build();
  }
}
