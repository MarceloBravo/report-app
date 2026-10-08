package com.saas.report.auth.domain.usuario;

public record AuthCredentials(
        String email,
        String passwordHash,
        String rol,
        boolean activo
) {
}