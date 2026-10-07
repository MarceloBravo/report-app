package com.saas.report.consulta.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ConvertirConsultaRequest(

        @NotBlank(message = "La pregunta del usuario es requerida")
        String preguntaUsuario,

        UUID dbConnectionId
) {
}
