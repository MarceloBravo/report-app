package com.saas.report.domain.valueObjects;

import com.saas.report.domain.exceptions.InvalidIntegerValue;

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