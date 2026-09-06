package com.saas.report.domain.valueObjects;

import com.saas.report.domain.exceptions.InvalidUUIDException;

import java.util.UUID;

public record PlanUUID(UUID value) {

    public PlanUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del plan no puede ser nulo");
        }
    }

    public static PlanUUID generate() {
        return new PlanUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}