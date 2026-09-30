package com.binitech.interview.adapters.outbound.llm;

import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.binitech.interview.domain.exception.LanguageModelUnavailableException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Traduz as falhas do SDK da Anthropic para a exceção de domínio. */
final class ClaudeCalls {

  private static final Logger log = LoggerFactory.getLogger(ClaudeCalls.class);

  private ClaudeCalls() {}

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
