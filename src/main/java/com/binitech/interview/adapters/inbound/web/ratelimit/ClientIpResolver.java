package com.binitech.interview.adapters.inbound.web.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Descobre o IP do cliente atrás do proxy do Railway.
 *
 * <p>Cada proxy acrescenta o IP de quem o chamou ao final do X-Forwarded-For. Os valores à esquerda
 * vêm do próprio cliente e podem ser forjados, então o IP confiável é o que está {@code
 * trustedProxyHops} posições a partir da direita.
 */
public class ClientIpResolver {

  private final int trustedProxyHops;

  public ClientIpResolver(int trustedProxyHops) {
    this.trustedProxyHops = trustedProxyHops;
  }

  public String resolve(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (trustedProxyHops <= 0 || forwardedFor == null || forwardedFor.isBlank()) {
      return request.getRemoteAddr();
    }
    String[] hops = forwardedFor.split(",");
    int index = Math.max(0, hops.length - trustedProxyHops);
    return hops[index].strip();
  }
}
