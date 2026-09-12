package com.saas.report.tenant.domain.connection;

import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.util.Optional;

public interface TenantDbConnectionRepository {

    Optional<TenantDbConnection> findById(TenantDbConnectionUUID dbConnectionId);

    Optional<TenantDbConnection> findByTenantId(TenantUUID tenantId);

    TenantDbConnection save(TenantDbConnection connection);
}