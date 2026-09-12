package com.saas.report.tenant.domain.schema;

import com.saas.report.tenant.domain.connection.TenantDbConnection;

public interface SchemaExtractor {

    EsquemaTenant extraer(TenantDbConnection conexion);
}