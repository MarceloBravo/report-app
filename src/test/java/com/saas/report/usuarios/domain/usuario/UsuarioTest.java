package com.saas.report.usuarios.domain.usuario;

import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
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
    void construyeUsuarioPlataformaSinTenant() {
        Usuario usuario = new Usuario(UsuarioUUID.generate(), null, "Pam", "pam@saas.com",
                "password-hash", UsuarioRol.PLATFORM_ADMIN, true);

        assertThat(usuario.getTenantId()).isNull();
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.PLATFORM_ADMIN);
        assertThat(usuario.esAdmin()).isFalse();
    }

    @Test
    void construyeUsuarioPlataformaUserSinTenant() {
        Usuario usuario = new Usuario(UsuarioUUID.generate(), null, "Op", "op@saas.com",
                "password-hash", UsuarioRol.PLATFORM_USER, true);

        assertThat(usuario.getTenantId()).isNull();
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.PLATFORM_USER);
        assertThat(usuario.esAdmin()).isFalse();
    }

    @Test
    void rechazaRolPlataformaConTenant() {
        assertThatThrownBy(() -> new Usuario(UsuarioUUID.generate(), TenantUUID.generate(),
                "Pam", "pam@saas.com", "password-hash", UsuarioRol.PLATFORM_ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(UsuarioUUID.generate(), TenantUUID.generate(),
                "Op", "op@saas.com", "password-hash", UsuarioRol.PLATFORM_USER, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaRolTenantSinTenant() {
        assertThatThrownBy(() -> new Usuario(UsuarioUUID.generate(), null,
                "Ana", "ana@empresa.com", "password-hash", UsuarioRol.VIEWER, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Usuario(UsuarioUUID.generate(), null,
                "Ana", "ana@empresa.com", "password-hash", UsuarioRol.ADMIN, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiarRolRespetaCompatibilidadConTenant() {
        Usuario admin = usuario(UsuarioRol.ADMIN, true);
        assertThatThrownBy(() -> admin.cambiarRol(UsuarioRol.PLATFORM_ADMIN, admin.getTenantId()))
                .isInstanceOf(IllegalArgumentException.class);

        Usuario plataforma = new Usuario(UsuarioUUID.generate(), null, "Pam", "pam@saas.com",
                "password-hash", UsuarioRol.PLATFORM_ADMIN, true);
        assertThatThrownBy(() -> plataforma.cambiarRol(UsuarioRol.ADMIN, plataforma.getTenantId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> admin.cambiarRol(null, admin.getTenantId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaElRol() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.cambiarRol(UsuarioRol.ADMIN, usuario.getTenantId());

        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.esAdmin()).isTrue();
    }

    @Test
    void cambiaElNombre() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.actualizarNombre("Bety");

        assertThat(usuario.getNombre()).isEqualTo("Bety");
        assertThatThrownBy(() -> usuario.actualizarNombre(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.actualizarNombre("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaElEmail() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.actualizarEmail("bety@empresa.com");

        assertThat(usuario.getEmail()).isEqualTo("bety@empresa.com");
        assertThatThrownBy(() -> usuario.actualizarEmail(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.actualizarEmail("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaLaPassword() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.actualizarPassword("nuevo-hash");

        assertThat(usuario.getPassword()).isEqualTo("nuevo-hash");
        assertThatThrownBy(() -> usuario.actualizarPassword(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> usuario.actualizarPassword("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiaElEstado() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.actualizarEstado(false);

        assertThat(usuario.getActivo()).isFalse();
        assertThat(usuario.isActivo()).isFalse();
    }

    @Test
    void verificaElEmail() {
        Usuario usuario = usuario(UsuarioRol.VIEWER, true);

        usuario.verificarEmail();

        assertThat(usuario.isEmailVerificado()).isTrue();
    }
}