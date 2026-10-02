package com.binitech.interview.adapters.outbound.llm;

import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.binitech.interview.domain.exception.LanguageModelUnavailableException;
import java.util.Locale;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ClaudeCalls {

  private static final Logger log = LoggerFactory.getLogger(ClaudeCalls.class);

  private ClaudeCalls() {}

  static <T> StructuredOutputConfig<T> outputConfig(Class<T> type, String effort) {
    StructuredOutputConfig.Builder<T> builder = StructuredOutputConfig.<T>builder().format(type);
    if (effort != null && !effort.isBlank()) {
      builder.effort(OutputConfig.Effort.of(effort.strip().toLowerCase(Locale.ROOT)));
    }
    return builder.build();
  }

  static <T> T execute(Supplier<T> call) {
    try {
      return call.get();
    } catch (AnthropicInvalidDataException e) {
      throw e;
    } catch (RateLimitException e) {
      log.warn("Rate limit da API da Anthropic atingido");
      throw new LanguageModelUnavailableException("Limite da API do modelo atingido", e);
    } catch (AnthropicServiceException e) {
      log.error("API da Anthropic respondeu com status {}", e.statusCode());
      throw new LanguageModelUnavailableException("API do modelo indisponível", e);
    } catch (AnthropicIoException e) {
      log.error("Falha de rede ao chamar a API da Anthropic: {}", e.getMessage());
      throw new LanguageModelUnavailableException("API do modelo inacessível", e);
    }
  }
}
