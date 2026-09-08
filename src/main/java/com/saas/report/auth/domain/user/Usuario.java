package com.saas.report.auth.domain.user;

import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

public class Usuario {

    private final UsuarioUUID usuarioId;
    private final TenantUUID tenantId;
    private final String nombre;
    private final String email;
    private final String password;
    private UsuarioRol rol;
    private boolean activo;

    public Usuario(UsuarioUUID usuarioId, TenantUUID tenantId, String nombre, String email, String password,
                   UsuarioRol rol, Boolean activo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        if (activo == null) {
            throw new IllegalArgumentException("El estado del usuario no puede ser nulo");
        }
        this.usuarioId = usuarioId;
        this.tenantId = tenantId;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public UsuarioUUID getUsuarioId() {
        return usuarioId;
    }

    public TenantUUID getTenantId() {
        return tenantId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UsuarioRol getRol() {
        return rol;
    }

    public boolean getActivo() {
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