package com.saas.report.infrastructure.persistence.mapper;

import com.saas.report.domain.entities.Tenant;
import com.saas.report.domain.valueObjects.TenantUUID;
import com.saas.report.infrastructure.persistence.TenantJpaEntity;

public final class TenantMapper {

    private TenantMapper() {
    }

    public static TenantJpaEntity toJpa(Tenant tenant) {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setTenantId(tenant.getTenantId().getValue());
        entity.setNombreEmpresa(tenant.getNombreEmpresa());
        entity.setSubdominioSlug(tenant.getSubdominioSlug());
        entity.setEstado(tenant.getEstado());
        entity.setFechaRegistro(tenant.getFechaRegistro());
        return entity;
    }

    public static Tenant toDomain(TenantJpaEntity entity) {
        return new Tenant(new TenantUUID(entity.getTenantId()),
                entity.getNombreEmpresa(), entity.getSubdominioSlug(), entity.getEstado());
    }
}