package com.saas.report.plan.application.dto;

import com.saas.report.plan.domain.Plan;

import java.util.List;
import java.util.UUID;

public record PlanResponse(
        UUID planId,
        String nombrePlan,
        String descripcion,
        Integer maxUsuarios,
        Integer limiteConsultasMensuales,
        Boolean activo,
        boolean esIlimitado,
        List<PlanPrecioResponse> precios
) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getPlanId().getValue(),
                plan.getNombrePlan(),
                plan.getDescripcion(),
                plan.getMaxUsuarios().getValue(),
                plan.getLimiteConsultasMensuales().getValue(),
                plan.isActivo(),
                plan.esIlimitado(),
                plan.getPrecios().stream().map(PlanPrecioResponse::from).toList());
    }
}