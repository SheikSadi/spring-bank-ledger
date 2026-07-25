package com.paypay.learn.ledger;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

  private static final String CORRELATION_HEADER = "X-Correlation-Id";
  private static final String MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String correlationId = request.getHeader(CORRELATION_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    
    MDC.put(MDC_KEY, correlationId);
    response.setHeader(CORRELATION_HEADER, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }

  }

}
