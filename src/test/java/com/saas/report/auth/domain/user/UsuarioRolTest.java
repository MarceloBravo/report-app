package com.saas.report.auth.domain.user;

import com.saas.report.identity.domain.user.UsuarioRol;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRolTest {

    @Test
    void usuarioRolDefineLosRolesEsperados() {
        assertThat(UsuarioRol.values()).containsExactly(
                UsuarioRol.ADMIN, UsuarioRol.VIEWER);
    }
}