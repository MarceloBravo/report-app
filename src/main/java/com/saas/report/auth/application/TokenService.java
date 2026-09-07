package com.saas.report.auth.application;

import com.saas.report.auth.domain.user.Usuario;

import java.time.LocalDateTime;

public interface TokenService {

    String generateAccessToken(Usuario usuario);

    String generateRefreshToken(Usuario usuario);

    boolean esTokenValido(String token);

    boolean esRefreshToken(String token);

    String extraerEmail(String token);

    String extraerUsuarioId(String token);

    String extraerTenantId(String token);

    String extraerRol(String token);

    LocalDateTime extraerExpiracion(String token);

    LocalDateTime extraerFechaCreacion(String token);
}