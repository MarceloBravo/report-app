package com.saas.report.tenant.infrastructure.schema;

import com.saas.report.tenant.domain.schema.EsquemaCache;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class EsquemaCacheService implements EsquemaCache {

    private final ConcurrentMap<TenantUUID, EsquemaTenant> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<EsquemaTenant> buscarPorTenant(TenantUUID tenantId) {
        return Optional.ofNullable(cache.get(tenantId));
    }

    @Override
    public void guardar(TenantUUID tenantId, EsquemaTenant esquema) {
        cache.put(tenantId, esquema);
    }

    @Override
    public void invalidar(TenantUUID tenantId) {
        cache.remove(tenantId);
    }
}