package com.saas.report.suscripcion.application.dto;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.suscripcion.domain.Suscripcion;

public record MiSuscripcionResponse(
        SuscripcionResponse suscripcion,
        PlanResponse plan
) {

    public static MiSuscripcionResponse from(Suscripcion suscripcion, Plan plan) {
        return new MiSuscripcionResponse(SuscripcionResponse.from(suscripcion), PlanResponse.from(plan));
    }
}