package com.saas.report.auth.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;

import java.util.UUID;

public record UsuarioTokenUUID(UUID value) {

    public UsuarioTokenUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del token no puede ser nulo");
        }
    }

    public static UsuarioTokenUUID generate() {
        return new UsuarioTokenUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}