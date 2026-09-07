package com.saas.report.domain.valueObjects;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record SuscripcionUUID(UUID value) {

    public SuscripcionUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID de la suscripción no puede ser nulo");
        }
    }

    public static SuscripcionUUID generate() {
        return new SuscripcionUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}