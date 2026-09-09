package com.saas.report.usuarios.domain.usuario;

import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
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
    void cambiaElNombre() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarNombre("Bety");

        assertThat(usuario.getNombre()).isEqualTo("Bety");
        assertThatThrownBy(() -> usuario.cambiarNombre(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.cambiarNombre("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaElEmail() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarEmail("bety@empresa.com");

        assertThat(usuario.getEmail()).isEqualTo("bety@empresa.com");
        assertThatThrownBy(() -> usuario.cambiarEmail(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.cambiarEmail("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaLaPassword() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarPassword("nuevo-hash");

        assertThat(usuario.getPassword()).isEqualTo("nuevo-hash");
        assertThatThrownBy(() -> usuario.cambiarPassword(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.cambiarPassword("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deshabilitaAlUsuario() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);
        usuario.deshabilitar();

        assertThat(usuario.getActivo()).isFalse();
    }

    @Test
    void cambiaElEstado() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarEstado(false);

        assertThat(usuario.getActivo()).isFalse();
    }
}