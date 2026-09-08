package com.saas.report.tenant.application.dto;

import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID tenantId,
        String nombreEmpresa,
        String subdominioSlug,
        TenantEstado estado,
        LocalDateTime fechaRegistro
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getTenantId().getValue(),
                tenant.getNombreEmpresa(),
                tenant.getSubdominioSlug(),
                tenant.getEstado(),
                tenant.getFechaRegistro());
    }
}