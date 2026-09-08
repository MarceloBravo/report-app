package com.saas.report.suscripcion.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SuscripcionRequest(

        @NotNull(message = "El tenant es requerido")
        UUID tenantId,

        @NotNull(message = "El plan es requerido")
        UUID planId,

        LocalDate fechaInicio
) {
}