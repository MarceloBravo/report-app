package com.saas.report.domain.valueObjects;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record ConsultaHistorialUUID(UUID value) {

    public ConsultaHistorialUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del historial de consultas no puede ser nulo");
        }
    }

    public static ConsultaHistorialUUID generate() {
        return new ConsultaHistorialUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}