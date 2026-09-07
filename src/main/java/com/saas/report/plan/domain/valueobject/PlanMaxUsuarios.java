package com.saas.report.plan.domain.valueobject;

import com.saas.report.shared.exception.InvalidIntegerValue;

public record PlanMaxUsuarios(Integer value) {

    public PlanMaxUsuarios {
        if (value == null || value < 0) {
            throw new InvalidIntegerValue("El máximo de usuarios no puede ser nulo o negativo");
        }
    }

    public Integer getValue() {
        return value;
    }

}
