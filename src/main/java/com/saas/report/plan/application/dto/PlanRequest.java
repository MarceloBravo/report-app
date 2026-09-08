package com.saas.report.plan.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlanRequest(

        @NotBlank(message = "El nombre del plan es requerido")
        @Size(max = 60, message = "El nombre del plan no puede superar los 60 caracteres")
        String nombrePlan,

        @NotBlank(message = "La descripción del plan es requerida")
        String descripcion,

        @NotNull(message = "El máximo de usuarios es requerido")
        @Min(value = 0, message = "El máximo de usuarios no puede ser negativo")
        Integer maxUsuarios,

        @NotNull(message = "El límite de consultas mensuales es requerido")
        @Min(value = -1, message = "El límite de consultas mensuales debe ser >= 0 o -1 (ilimitado)")
        Integer limiteConsultasMensuales,

        @NotNull(message = "El estado de activación es requerido")
        Boolean activo,

        @NotEmpty(message = "El plan debe definir al menos un tramo de precio")
        List<@Valid PlanPrecioRequest> precios
) {
}