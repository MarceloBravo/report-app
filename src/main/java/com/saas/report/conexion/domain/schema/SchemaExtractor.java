package com.saas.report.conexion.domain.schema;

import com.saas.report.conexion.domain.connection.TenantDbConnection;

public interface SchemaExtractor {

    EsquemaTenant extraer(TenantDbConnection conexion);
}