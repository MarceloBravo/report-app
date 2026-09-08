package com.saas.report.plan.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlanPrecioRequest(

        @NotNull(message = "El mes de inicio del tramo es requerido")
        @Min(value = 1, message = "El mes de inicio del tramo debe ser >= 1")
        Integer mesDesde,

        Integer mesHasta,

        @NotNull(message = "El precio del tramo es requerido")
        @DecimalMin(value = "0.0", message = "El precio del tramo no puede ser negativo")
        BigDecimal precio
) {
}