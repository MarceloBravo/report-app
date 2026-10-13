package com.saas.report.conexion.infrastructure.persistence.mapper;

import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.conexion.infrastructure.persistence.TenantDbConnectionJpaEntity;

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
        entity.setMotorBd(connection.getMotorBd());
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
                entity.getSchemaName(),
                entity.getMotorBd() != null ? entity.getMotorBd() : MotorBaseDatos.POSTGRESQL);
    }
}