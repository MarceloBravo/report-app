package com.saas.report.consulta.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConvertirConsultaRequest(

        @NotNull(message = "El tenant es requerido")
        UUID tenantId,

        @NotNull(message = "El usuario es requerido")
        UUID usuarioId,

        @NotBlank(message = "La pregunta del usuario es requerida")
        String preguntaUsuario,

        UUID dbConnectionId
) {
}