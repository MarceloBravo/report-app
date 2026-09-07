package com.saas.report.plan.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record PlanPrecioUUID(UUID value) {

    public PlanPrecioUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del tramo de precio no puede ser nulo");
        }
    }

    public static PlanPrecioUUID generate() {
        return new PlanPrecioUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}