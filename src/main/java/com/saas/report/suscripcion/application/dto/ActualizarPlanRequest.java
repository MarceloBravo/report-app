package com.saas.report.suscripcion.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActualizarPlanRequest(

        @NotNull(message = "El plan es requerido")
        UUID planId
) {
}