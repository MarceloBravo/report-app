package com.saas.report.tenant.infrastructure.persistence.mapper;

import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.connection.TipoConexion;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.tenant.infrastructure.persistence.TenantDbConnectionJpaEntity;

public final class TenantDbConnectionMapper {

    private TenantDbConnectionMapper() {
    }

    public static TenantDbConnectionJpaEntity toJpa(TenantDbConnection connection) {
        TenantDbConnectionJpaEntity entity = new TenantDbConnectionJpaEntity();
        entity.setDbConnectionId(connection.getDbConnectionId().getValue());
        entity.setTenantId(connection.getTenantId().getValue());
        entity.setHost(connection.getHost());
        entity.setPuerto(connection.getPuerto());
        entity.setDbName(connection.getDbName());
        entity.setDbUser(connection.getDbUser());
        entity.setDbPasswordEncrypted(connection.getDbPasswordEncrypted());
        entity.setTipoConexion(connection.getTipoConexion());
        entity.setSchemaName(connection.getSchemaName());
        return entity;
    }

    public static TenantDbConnection toDomain(TenantDbConnectionJpaEntity entity) {
        return new TenantDbConnection(
                new TenantDbConnectionUUID(entity.getDbConnectionId()),
                new TenantUUID(entity.getTenantId()),
                entity.getHost(),
                entity.getPuerto(),
                entity.getDbName(),
                entity.getDbUser(),
                entity.getDbPasswordEncrypted(),
                entity.getTipoConexion() != null ? entity.getTipoConexion() : TipoConexion.EXTERNAL_DB,
                entity.getSchemaName());
    }
}