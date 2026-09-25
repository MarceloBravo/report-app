package com.saas.report.tenant.infrastructure.schema.factory;

import com.saas.report.tenant.infrastructure.schema.strategy.MySqlSchemaQueryProviderStrategy;
import com.saas.report.tenant.infrastructure.schema.strategy.PostgresqlSchemaQueryProviderStrategy;

public class SchemaQueryFactory {
    
    public static String getSchemaQuery(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return new MySqlSchemaQueryProviderStrategy().getSchemaQuery();
            case "postgresql":
                return new PostgresqlSchemaQueryProviderStrategy().getSchemaQuery();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}
