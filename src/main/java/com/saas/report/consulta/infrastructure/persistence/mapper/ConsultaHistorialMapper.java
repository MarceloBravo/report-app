package com.saas.report.consulta.infrastructure.persistence.mapper;

import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.consulta.infrastructure.persistence.ConsultaHistorialJpaEntity;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;

public final class ConsultaHistorialMapper {

    private ConsultaHistorialMapper() {
    }

    public static ConsultaHistorialJpaEntity toJpa(ConsultaHistorial consulta) {
        ConsultaHistorialJpaEntity entity = new ConsultaHistorialJpaEntity();
        entity.setConsultaId(consulta.getConsultaId().getValue());
        entity.setTenantId(consulta.getTenantId().getValue());
        entity.setUsuarioId(consulta.getUsuarioId().getValue());
        entity.setPreguntaUsuario(consulta.getPreguntaUsuario());
        entity.setQuerySqlGenerada(consulta.getQuerySqlGenerada());
        entity.setFechaConsulta(consulta.getFechaConsulta());
        entity.setExitosa(consulta.getExitosa());
        entity.setRegistrosCount(consulta.getRegistrosCount());
        entity.setDbConnectionId(consulta.getDbConnectionId() != null
                ? consulta.getDbConnectionId().getValue() : null);
        entity.setConsultasEnPeriodo(consulta.getConsultasEnPeriodo());
        entity.setMotorBd(consulta.getMotorBd());
        return entity;
    }

    public static ConsultaHistorial toDomain(ConsultaHistorialJpaEntity entity) {
        return new ConsultaHistorial(
                new ConsultaHistorialUUID(entity.getConsultaId()),
                new TenantUUID(entity.getTenantId()),
                new UsuarioUUID(entity.getUsuarioId()),
                entity.getPreguntaUsuario(),
                entity.getQuerySqlGenerada(),
                entity.getFechaConsulta(),
                entity.getExitosa(),
                entity.getRegistrosCount(),
                entity.getDbConnectionId() != null
                        ? new TenantDbConnectionUUID(entity.getDbConnectionId()) : null,
                entity.getConsultasEnPeriodo(),
                entity.getMotorBd());
    }
}