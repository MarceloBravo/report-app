package com.saas.report.auth.domain.usuario;

import java.util.UUID;

public record AuthUser(
        UUID id,
        String email,
        String nombre,
        String rol,
        UUID tenantId
) {
}