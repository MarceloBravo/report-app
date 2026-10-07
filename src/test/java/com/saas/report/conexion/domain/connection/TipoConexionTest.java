package com.saas.report.conexion.domain.connection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoConexionTest {

    @Test
    void defineLosTiposEsperados() {
        assertThat(TipoConexion.values()).containsExactly(
                TipoConexion.EXTERNAL_DB, TipoConexion.INTERNAL_SCHEMA);
    }
}