package com.binitech.interview.config;

import com.binitech.interview.adapters.inbound.web.ratelimit.ClientIpResolver;
import com.binitech.interview.adapters.inbound.web.ratelimit.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfig {

  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
      InterviewProperties properties, ObjectMapper objectMapper) {
    InterviewProperties.RateLimit rateLimit = properties.rateLimit();
    RateLimitFilter filter =
        new RateLimitFilter(
            new ClientIpResolver(rateLimit.trustedProxyHops()),
            objectMapper,
            rateLimit.perClientPerMinute(),
            rateLimit.perClientPerDay(),
            rateLimit.globalPerDay());
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registration;
  }
}
