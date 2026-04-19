package com.algorycode.rent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestIdMdcFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Request-Id";
  public static final String MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String rid = request.getHeader(HEADER);
    if (rid == null || rid.isBlank()) {
      rid = UUID.randomUUID().toString();
    } else {
      rid = rid.trim();
      if (rid.length() > 128) {
        rid = rid.substring(0, 128);
      }
    }
    MDC.put(MDC_KEY, rid);
    response.setHeader(HEADER, rid);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
