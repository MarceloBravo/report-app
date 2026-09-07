package com.saas.report.auth.domain.valueobject;

import com.saas.report.domain.exceptions.InvalidUUIDException;

import java.util.UUID;

public record UsuarioUUID(UUID value) {

    public UsuarioUUID {
        if (value == null) {
            throw new InvalidUUIDException("El ID del usuario no puede ser nulo");
        }
    }

    public static UsuarioUUID generate() {
        return new UsuarioUUID(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

}