package com.saas.report.onboarding.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.report.shared.presentation.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class SelfServeRateLimitFilter extends OncePerRequestFilter {

    private final SelfServeRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public SelfServeRateLimitFilter(SelfServeRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getMethod().equals("POST")
                || !"/self-serve/registro".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimiter.intentarConsumir(obtenerIpCliente(request))) {
            ErrorResponse body = new ErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Demasiadas solicitudes de registro. Intente más tarde.",
                    request.getServletPath(),
                    LocalDateTime.now());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}