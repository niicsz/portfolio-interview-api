package com.binitech.interview.config;

import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter(InterviewProperties properties) {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(properties.allowedOrigins());
    cors.setAllowedMethods(List.of("POST", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Content-Type"));
    cors.setExposedHeaders(List.of("Retry-After"));
    cors.setAllowCredentials(false);
    cors.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/interview/**", cors);

    FilterRegistrationBean<CorsFilter> registration =
        new FilterRegistrationBean<>(new CorsFilter(source));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
