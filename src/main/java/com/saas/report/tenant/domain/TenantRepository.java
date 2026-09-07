package com.saas.report.tenant.domain;

import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findById(TenantUUID tenantId);

    boolean existsById(TenantUUID tenantId);

    Tenant save(Tenant tenant);
}