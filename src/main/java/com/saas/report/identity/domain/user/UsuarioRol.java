package com.saas.report.identity.domain.user;

public enum UsuarioRol {
    PLATFORM_ADMIN,
    PLATFORM_USER,
    ADMIN,
    VIEWER;

    public boolean esRolPlataforma() {
        return this == PLATFORM_ADMIN || this == PLATFORM_USER;
    }

    public boolean esRolTenant() {
        return this == ADMIN || this == VIEWER;
    }

    public boolean puedeCrear(UsuarioRol nuevoRol) {
        if (nuevoRol == null) {
            return false;
        }
        return switch (this) {
            case PLATFORM_ADMIN -> true;
            case PLATFORM_USER -> nuevoRol == ADMIN || nuevoRol == VIEWER;
            case ADMIN -> nuevoRol == VIEWER;
            case VIEWER -> false;
        };
    }

    public boolean puedeModificar(UsuarioRol otroRol) {
        if (otroRol == null) {
            return false;
        }
        return switch (this) {
            case PLATFORM_ADMIN -> true;
            case PLATFORM_USER -> otroRol == ADMIN || otroRol == VIEWER;
            case ADMIN -> otroRol == VIEWER;
            case VIEWER -> false;
        };
    }
}