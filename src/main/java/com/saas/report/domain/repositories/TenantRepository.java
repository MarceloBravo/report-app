package com.saas.report.domain.repositories;

import com.saas.report.domain.entities.Tenant;
import com.saas.report.domain.valueObjects.TenantUUID;

import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findById(TenantUUID tenantId);

    boolean existsById(TenantUUID tenantId);

    Tenant save(Tenant tenant);
}