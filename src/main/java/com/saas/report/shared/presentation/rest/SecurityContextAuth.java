package com.saas.report.shared.presentation.rest;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityContextAuth {

    private SecurityContextAuth() {
    }

    public static AuthPrincipal principalActual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("No autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new ForbiddenException("No autenticado");
        }
        return authPrincipal;
    }

    public static TenantUUID tenantIdActual() {
        String tenantId = principalActual().tenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ForbiddenException("El usuario autenticado no pertenece a un tenant");
        }
        try {
            return new TenantUUID(UUID.fromString(tenantId));
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenException("El tenant del usuario autenticado no es válido");
        }
    }
}