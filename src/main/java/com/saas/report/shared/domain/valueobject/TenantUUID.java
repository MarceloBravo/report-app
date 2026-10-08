package com.saas.report.shared.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

// Value Object para la identidad
public record TenantUUID(UUID value) {

    public TenantUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID no puede ser nulo");
        }
    }

    public static TenantUUID generate() {
        return new TenantUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}
