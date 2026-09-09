package com.saas.report.identity.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRolTest {

    @Test
    void usuarioRolDefineLosRolesEsperados() {
        assertThat(UsuarioRol.values()).containsExactly(
                UsuarioRol.PLATFORM_ADMIN, UsuarioRol.PLATFORM_USER, UsuarioRol.ADMIN, UsuarioRol.VIEWER);
    }

    @Test
    void clasificaRolesDePlataformaYDeTenant() {
        assertThat(UsuarioRol.PLATFORM_ADMIN.esRolPlataforma()).isTrue();
        assertThat(UsuarioRol.PLATFORM_USER.esRolPlataforma()).isTrue();
        assertThat(UsuarioRol.ADMIN.esRolTenant()).isTrue();
        assertThat(UsuarioRol.VIEWER.esRolTenant()).isTrue();
        assertThat(UsuarioRol.ADMIN.esRolPlataforma()).isFalse();
        assertThat(UsuarioRol.VIEWER.esRolPlataforma()).isFalse();
    }

    @Test
    void puedeCrearSegunMatrizDeRoles() {
        assertThat(UsuarioRol.PLATFORM_ADMIN.puedeCrear(UsuarioRol.PLATFORM_ADMIN)).isTrue();
        assertThat(UsuarioRol.PLATFORM_ADMIN.puedeCrear(UsuarioRol.PLATFORM_USER)).isTrue();
        assertThat(UsuarioRol.PLATFORM_ADMIN.puedeCrear(UsuarioRol.ADMIN)).isTrue();
        assertThat(UsuarioRol.PLATFORM_ADMIN.puedeCrear(UsuarioRol.VIEWER)).isTrue();

        assertThat(UsuarioRol.PLATFORM_USER.puedeCrear(UsuarioRol.PLATFORM_ADMIN)).isFalse();
        assertThat(UsuarioRol.PLATFORM_USER.puedeCrear(UsuarioRol.PLATFORM_USER)).isFalse();
        assertThat(UsuarioRol.PLATFORM_USER.puedeCrear(UsuarioRol.ADMIN)).isTrue();
        assertThat(UsuarioRol.PLATFORM_USER.puedeCrear(UsuarioRol.VIEWER)).isTrue();

        assertThat(UsuarioRol.ADMIN.puedeCrear(UsuarioRol.ADMIN)).isFalse();
        assertThat(UsuarioRol.ADMIN.puedeCrear(UsuarioRol.VIEWER)).isTrue();

        assertThat(UsuarioRol.VIEWER.puedeCrear(UsuarioRol.VIEWER)).isFalse();
        assertThat(UsuarioRol.ADMIN.puedeCrear(null)).isFalse();
    }
}