package com.saas.report.suscripcion.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuscripcionEstadoTest {

    @Test
    void suscripcionEstadoDefineLosEstadosEsperados() {
        assertThat(SuscripcionEstado.values()).containsExactly(
                SuscripcionEstado.ACTIVE, SuscripcionEstado.CANCELED, SuscripcionEstado.SUSPENDED);
    }
}