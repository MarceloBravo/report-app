package com.saas.report.plan.application.dto;

import com.saas.report.plan.domain.PlanPrecio;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanPrecioResponse(
        UUID planPrecioId,
        Integer mesDesde,
        Integer mesHasta,
        BigDecimal precio
) {

    public static PlanPrecioResponse from(PlanPrecio precio) {
        return new PlanPrecioResponse(
                precio.getPlanPrecioId().getValue(),
                precio.getMesDesde(),
                precio.getMesHasta(),
                precio.getPrecio());
    }
}