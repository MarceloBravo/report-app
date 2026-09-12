package com.saas.report.onboarding.infrastructure.security;

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

    private static final String MESSAGE = "Demasiadas solicitudes de registro. Intente más tarde.";

    private final SelfServeRateLimiter rateLimiter;

    public SelfServeRateLimitFilter(SelfServeRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
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
            String body = "{\"status\":" + HttpStatus.TOO_MANY_REQUESTS.value()
                    + ",\"error\":\"" + HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase() + "\""
                    + ",\"message\":\"" + MESSAGE + "\""
                    + ",\"path\":\"" + escapar(request.getServletPath()) + "\""
                    + ",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(body);
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

    private String escapar(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}