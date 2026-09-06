package com.saas.report.domain.valueObjects;

import com.saas.report.domain.exceptions.InvalidUUIDException;

import java.util.UUID;

// Value Object para la identidad
public final class TenantUUID {
    private final UUID value;

    public TenantUUID(UUID value) {
        if (value == null) {
            throw new InvalidUUIDException("El ID no puede ser nulo");
        }
        this.value = value;
    }

    public static TenantUUID generate() {
        return new TenantUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }
}
