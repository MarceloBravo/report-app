package com.saas.report.domain.entities;

import com.saas.report.domain.enums.TenantEstado;
import com.saas.report.domain.enums.UsuarioRol;
import com.saas.report.domain.valueObjects.TenantUUID;
import com.saas.report.domain.valueObjects.UsuarioUUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    private Usuario usuario(UsuarioRol rol, Boolean activo) {
        return new Usuario(UsuarioUUID.generate(), new Tenant(TenantUUID.generate(), "Empresa", "empresa",
                TenantEstado.ACTIVE), "Ana", "ana@empresa.com", rol, activo);
    }

    @Test
    void construyeUsuarioYExponeDatos() {
        Tenant tenant = new Tenant(TenantUUID.generate(), "Empresa", "empresa", TenantEstado.ACTIVE);
        UsuarioUUID id = UsuarioUUID.generate();
        Usuario usuario = new Usuario(id, tenant, "Ana", "ana@empresa.com", UsuarioRol.ADMIN, true);

        assertThat(usuario.getUsuarioId()).isEqualTo(id);
        assertThat(usuario.getTenant()).isEqualTo(tenant);
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.getActivo()).isTrue();
    }

    @Test
    void rechazaDatosInvalidos() {
        Tenant tenant = new Tenant(TenantUUID.generate(), "Empresa", "empresa", TenantEstado.ACTIVE);
        UsuarioUUID id = UsuarioUUID.generate();

        assertThatThrownBy(() -> new Usuario(null, tenant, "Ana", "ana@e.com", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, null, "Ana", "ana@e.com", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, null, "ana@e.com", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "  ", "ana@e.com", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", null, UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "  ", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(id, tenant, "Ana", "ana@e.com", null, true))
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