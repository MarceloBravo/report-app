package com.saas.report.consulta.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConsultaRequest(

        @NotNull(message = "El tenant es requerido")
        UUID tenantId,

        @NotNull(message = "El usuario es requerido")
        UUID usuarioId,

        @NotBlank(message = "La pregunta del usuario es requerida")
        String preguntaUsuario,

        @NotBlank(message = "El SQL generado es requerido")
        String querySqlGenerada,

        Boolean exitosa,

        Integer registrosCount,

        UUID dbConnectionId
) {
}