package com.bell_ringer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class RequestLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingConfig.class);
    private static final List<String> EXCLUDE_EXTENSIONS = Arrays.asList(".css", ".js", ".png");

    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
                String uri = request.getRequestURI().toLowerCase();
                return EXCLUDE_EXTENSIONS.stream().anyMatch(uri::endsWith);
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
                try {
                    filterChain.doFilter(wrappedRequest, response);
                } finally {
                    logRequest(wrappedRequest);
                }
            }

            private void logRequest(ContentCachingRequestWrapper request) {
                try {
                    String method = request.getMethod();
                    String uri = request.getRequestURI();
                    String payload = "";
                    byte[] buf = request.getContentAsByteArray();
                    if (buf.length > 0) {
                        payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                    }
                    logger.info("Incoming request: method={}, uri={}, payload={}");
                } catch (Exception e) {
                    logger.warn("Failed to log request body", e);
                }
            }
        };
    }
}
