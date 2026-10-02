package com.binitech.interview.adapters.inbound.web.ratelimit;

import com.binitech.interview.adapters.inbound.web.generated.api.InterviewApi;
import com.binitech.interview.adapters.inbound.web.generated.model.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

  private final ClientIpResolver clientIpResolver;
  private final ObjectMapper objectMapper;
  private final int perClientPerMinute;
  private final int perClientPerDay;
  private final Bucket globalBucket;
  private final Cache<String, Bucket> clientBuckets =
      Caffeine.newBuilder().maximumSize(100_000).expireAfterAccess(Duration.ofDays(1)).build();

  public RateLimitFilter(
      ClientIpResolver clientIpResolver,
      ObjectMapper objectMapper,
      int perClientPerMinute,
      int perClientPerDay,
      int globalPerDay) {
    this.clientIpResolver = clientIpResolver;
    this.objectMapper = objectMapper;
    this.perClientPerMinute = perClientPerMinute;
    this.perClientPerDay = perClientPerDay;
    this.globalBucket =
        Bucket.builder()
            .addLimit(
                limit ->
                    limit.capacity(globalPerDay).refillIntervally(globalPerDay, Duration.ofDays(1)))
            .build();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !("POST".equals(request.getMethod())
        && InterviewApi.PATH_ASK_QUESTION.equals(request.getRequestURI()));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Bucket clientBucket =
        clientBuckets.get(clientIpResolver.resolve(request), ip -> newClientBucket());

    ConsumptionProbe clientProbe = clientBucket.tryConsumeAndReturnRemaining(1);
    if (!clientProbe.isConsumed()) {
      reject(response, clientProbe, "Muitas perguntas em pouco tempo. Tente novamente mais tarde.");
      return;
    }
    ConsumptionProbe globalProbe = globalBucket.tryConsumeAndReturnRemaining(1);
    if (!globalProbe.isConsumed()) {
      log.warn("Limite diário global de perguntas atingido");
      reject(response, globalProbe, "O assistente atingiu o limite diário. Volte amanhã.");
      return;
    }
    chain.doFilter(request, response);
  }

  private Bucket newClientBucket() {
    return Bucket.builder()
        .addLimit(
            limit ->
                limit
                    .capacity(perClientPerMinute)
                    .refillGreedy(perClientPerMinute, Duration.ofMinutes(1)))
        .addLimit(
            limit ->
                limit
                    .capacity(perClientPerDay)
                    .refillIntervally(perClientPerDay, Duration.ofDays(1)))
        .build();
  }

  private void reject(HttpServletResponse response, ConsumptionProbe probe, String message)
      throws IOException {
    long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("Retry-After", String.valueOf(retryAfter));
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(
        response.getOutputStream(),
        new ErrorDTO(HttpStatus.TOO_MANY_REQUESTS.value(), message, OffsetDateTime.now()));
  }
}
