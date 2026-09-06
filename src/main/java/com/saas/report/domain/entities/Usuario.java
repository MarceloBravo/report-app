package com.saas.report.domain.entities;

import com.saas.report.domain.enums.UsuarioRol;
import com.saas.report.domain.valueObjects.UsuarioUUID;

public class Usuario {

    private final UsuarioUUID usuarioId;
    private final Tenant tenant;
    private final String nombre;
    private final String email;
    private UsuarioRol rol;
    private Boolean activo;

    public Usuario(UsuarioUUID usuarioId, Tenant tenant, String nombre, String email, UsuarioRol rol, Boolean activo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        this.usuarioId = usuarioId;
        this.tenant = tenant;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public UsuarioUUID getUsuarioId() {
        return usuarioId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public UsuarioRol getRol() {
        return rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public boolean esAdmin() {
        return this.rol == UsuarioRol.ADMIN;
    }

    public void cambiarRol(UsuarioRol nuevoRol) {
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El nuevo rol no puede ser nulo");
        }
        this.rol = nuevoRol;
    }

    public void deshabilitar() {
        this.activo = false;
    }
}