package com.saas.report.usuarios.domain.usuario;

import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

public class Usuario {

    private final UsuarioUUID usuarioId;
    private final TenantUUID tenantId;
    private String nombre;
    private String email;
    private String password;
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

    public void cambiarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nuevoNombre;
    }

    public void cambiarEmail(String nuevoEmail) {
        if (nuevoEmail == null || nuevoEmail.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        this.email = nuevoEmail;
    }

    public void cambiarPassword(String nuevoPassword) {
        if (nuevoPassword == null || nuevoPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        this.password = nuevoPassword;
    }

    public void cambiarEstado(boolean nuevoEstado) {
        this.activo = nuevoEstado;
    }

    public void deshabilitar() {
        this.activo = false;
    }
}