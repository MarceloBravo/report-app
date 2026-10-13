package com.saas.report.consulta.application.dto;

import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.shared.presentation.mapper.MotorBaseDatosPresenter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;

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
        Integer consultasEnPeriodo,
        String motorBd,
        List<Map<String, Object>> data
) {

    public static ConsultaResponse from(ConsultaHistorial consulta, List<Map<String, Object>> data) {
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
                consulta.getConsultasEnPeriodo(),
                MotorBaseDatosPresenter.nombreAMostrar(consulta.getMotorBd()),
                data
        );
    }

    public static ConsultaResponse from(ConsultaHistorial consulta) {
        return from(consulta, List.of());
    }
}