package com.saas.report.tenant.domain.schema;

import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.util.Optional;

public interface EsquemaCache {

    Optional<EsquemaTenant> buscarPorTenant(TenantUUID tenantId);

    void guardar(TenantUUID tenantId, EsquemaTenant esquema);

    void invalidar(TenantUUID tenantId);
}