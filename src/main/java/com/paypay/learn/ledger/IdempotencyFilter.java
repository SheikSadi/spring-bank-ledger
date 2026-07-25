package com.paypay.learn.ledger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyFilter.class);

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private IdempotencyRepository repo;

    public IdempotencyFilter(
        IdempotencyRepository repo
    ) {
        this.repo = repo;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String idempotencyId = request.getHeader(IDEMPOTENCY_HEADER);
        
        if (
            "GET".equalsIgnoreCase(request.getMethod())
            || idempotencyId == null
            || idempotencyId.isBlank()
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyEntry> existing = repo.findById(idempotencyId);

        if (existing.isPresent()) {
            IdempotencyEntry entry = existing.get();
            response.setStatus(entry.statusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(entry.responseBody());
            return;
        }
        
        // Idempotency key not found; we need to cache the response
        // The controller writes the HTTP response to the wrapper
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(request, wrapper);

        int statusCode = wrapper.getStatus();
        byte[] buffer = wrapper.getContentAsByteArray();
        String responseBody = new String(buffer, StandardCharsets.UTF_8);
        LocalDateTime createdAt = LocalDateTime.now();

        if (statusCode >= 200 && statusCode < 300) {
            // Cache the buffered response before returning
            try {
                repo.save(
                    new IdempotencyEntry(
                        idempotencyId, statusCode, responseBody, createdAt
                    )
                );
            } catch (Exception ex) {
                logger.error(
                    "Unexpected error occured when saving to IdempotencyRepository", ex
                );
            }
        }

        // Now copy the buffered bytes into the network output stream 
        wrapper.copyBodyToResponse();
    }
}
