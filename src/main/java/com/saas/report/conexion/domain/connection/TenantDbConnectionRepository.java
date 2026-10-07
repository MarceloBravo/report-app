package com.saas.report.conexion.domain.connection;

import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.util.List;
import java.util.Optional;

public interface TenantDbConnectionRepository {

    Optional<TenantDbConnection> findById(TenantDbConnectionUUID dbConnectionId);

    Optional<TenantDbConnection> findByTenantId(TenantUUID tenantId);

    List<TenantDbConnection> findAllByTenantId(TenantUUID tenantId);

    TenantDbConnection save(TenantDbConnection connection);
}