package com.saas.report.plan.domain.valueobject;

import com.saas.report.shared.exception.InvalidIntegerValue;

public record PlanLimiteConsultasMensuales(Integer value) {

    public PlanLimiteConsultasMensuales {
        if (value == null || value < -1) {
            throw new InvalidIntegerValue("El límite de consultas mensuales debe ser >= 0 o -1 (ilimitado)");
        }
    }

    public static PlanLimiteConsultasMensuales ilimitado() {
        return new PlanLimiteConsultasMensuales(-1);
    }

    public Integer getValue() {
        return value;
    }

    public boolean esIlimitado() {
        return value == -1;
    }
}