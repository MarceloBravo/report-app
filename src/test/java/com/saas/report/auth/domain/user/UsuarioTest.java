package com.saas.report.auth.domain.user;

import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    private Usuario usuario(UsuarioRol rol, Boolean activo) {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "password-hash", rol, activo);
    }

    @Test
    void construyeUsuarioYExponeDatos() {
        TenantUUID tenant = TenantUUID.generate();
        UsuarioUUID id = UsuarioUUID.generate();
        Usuario usuario = new Usuario(id, tenant, "Ana", "ana@empresa.com", "password-hash", UsuarioRol.ADMIN, true);

        assertThat(usuario.getUsuarioId()).isEqualTo(id);
        assertThat(usuario.getTenantId()).isEqualTo(tenant);
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(usuario.getPassword()).isEqualTo("password-hash");
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.getActivo()).isTrue();
    }

    @Test
    void rechazaDatosInvalidos() {
        TenantUUID tenant = TenantUUID.generate();
        UsuarioUUID id = UsuarioUUID.generate();

        assertThatThrownBy(() -> new Usuario(null, tenant, "Ana", "ana@e.com", "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, null, "Ana", "ana@e.com", "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, null, "ana@e.com", "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "  ", "ana@e.com", "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", null, "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "  ", "pw", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "ana@e.com", null, UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "ana@e.com", "  ", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "ana@e.com", "pw", null, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void esAdminSoloParaElRolAdmin() {
        assertThat(usuario(UsuarioRol.ADMIN, true).esAdmin()).isTrue();
        assertThat(usuario(UsuarioRol.VIEWER, true).esAdmin()).isFalse();
    }

    @Test
    void cambiaElRol() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarRol(UsuarioRol.ADMIN);

        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.esAdmin()).isTrue();
        assertThatThrownBy(() -> usuario.cambiarRol(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deshabilitaAlUsuario() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);
        usuario.deshabilitar();

        assertThat(usuario.getActivo()).isFalse();
    }
}