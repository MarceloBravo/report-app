package com.saas.report.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsTest {

    @Test
    void suscripcionEstadoDefineLosEstadosEsperados() {
        assertThat(SuscripcionEstado.values()).containsExactly(
                SuscripcionEstado.ACTIVE, SuscripcionEstado.CANCELED, SuscripcionEstado.SUSPENDED);
    }
}