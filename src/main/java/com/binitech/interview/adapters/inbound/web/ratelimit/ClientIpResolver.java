package com.binitech.interview.adapters.inbound.web.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

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
