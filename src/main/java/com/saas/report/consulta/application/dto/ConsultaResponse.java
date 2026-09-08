package com.saas.report.consulta.application.dto;

import com.saas.report.consulta.domain.ConsultaHistorial;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaResponse(
        UUID consultaId,
        UUID tenantId,
        UUID usuarioId,
        String preguntaUsuario,
        String querySqlGenerada,
        LocalDateTime fechaConsulta,
        Boolean exitosa,
        Integer registrosCount,
        UUID dbConnectionId,
        Integer consultasEnPeriodo
) {

    public static ConsultaResponse from(ConsultaHistorial consulta) {
        return new ConsultaResponse(
                consulta.getConsultaId().getValue(),
                consulta.getTenantId().getValue(),
                consulta.getUsuarioId().getValue(),
                consulta.getPreguntaUsuario(),
                consulta.getQuerySqlGenerada(),
                consulta.getFechaConsulta(),
                consulta.getExitosa(),
                consulta.getRegistrosCount(),
                consulta.getDbConnectionId() != null ? consulta.getDbConnectionId().getValue() : null,
                consulta.getConsultasEnPeriodo());
    }
}