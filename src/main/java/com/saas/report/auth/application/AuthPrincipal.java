package com.saas.report.auth.application;

public record AuthPrincipal(
        String email,
        String usuarioId,
        String tenantId,
        String rol
) {}
