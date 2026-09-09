package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.usuarios.domain.usuario.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioTokenRepository usuarioTokenRepository;

    public JwtAuthorizationFilter(TokenService tokenService,
                                  UsuarioTokenRepository usuarioTokenRepository) {
        this.tokenService = tokenService;
        this.usuarioTokenRepository = usuarioTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (tokenService.esTokenValido(token)) {
                usuarioTokenRepository.findByAccessToken(token).ifPresent(registro -> {
                    if (registro.estaActivo() && !registro.estaExpiradoAccessToken()) {
                        Usuario usuario = registro.getUsuario();
                        var authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                        AuthPrincipal principal = new AuthPrincipal(
                                usuario.getEmail(),
                                usuario.getUsuarioId().getValue().toString(),
                                usuario.getTenantId() != null
                                        ? usuario.getTenantId().getValue().toString()
                                        : null,
                                usuario.getRol().name());

                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, authorities);
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean esRutaPublica = path.startsWith("/auth/login")
                || path.startsWith("/auth/refresh");
        boolean esPreflight = "OPTIONS".equalsIgnoreCase(request.getMethod());
        return esRutaPublica || esPreflight;
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}