package com.saas.report.conexion.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record TenantDbConnectionUUID(UUID value) {

    public TenantDbConnectionUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID de la conexión externa no puede ser nulo");
        }
    }

    public static TenantDbConnectionUUID generate() {
        return new TenantDbConnectionUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}