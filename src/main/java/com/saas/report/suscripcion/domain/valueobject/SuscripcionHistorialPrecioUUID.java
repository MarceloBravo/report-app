package com.saas.report.suscripcion.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record SuscripcionHistorialPrecioUUID(UUID value) {

    public SuscripcionHistorialPrecioUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del historial de precios no puede ser nulo");
        }
    }

    public static SuscripcionHistorialPrecioUUID generate() {
        return new SuscripcionHistorialPrecioUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}