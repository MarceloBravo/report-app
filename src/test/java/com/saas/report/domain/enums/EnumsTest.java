package com.saas.report.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsTest {

    @Test
    void tenantEstadoDefineLosEstadosEsperados() {
        assertThat(TenantEstado.values()).containsExactly(
                TenantEstado.ACTIVE, TenantEstado.SUSPENDED, TenantEstado.CANCELED);
    }

    @Test
    void suscripcionEstadoDefineLosEstadosEsperados() {
        assertThat(SuscripcionEstado.values()).containsExactly(
                SuscripcionEstado.ACTIVE, SuscripcionEstado.CANCELED, SuscripcionEstado.SUSPENDED);
    }

    @Test
    void tipoConexionDefineLosTiposEsperados() {
        assertThat(TipoConexion.values()).containsExactly(
                TipoConexion.EXTERNAL_DB, TipoConexion.INTERNAL_SCHEMA);
    }

    @Test
    void usuarioRolDefineLosRolesEsperados() {
        assertThat(UsuarioRol.values()).containsExactly(
                UsuarioRol.ADMIN, UsuarioRol.VIEWER);
    }
}