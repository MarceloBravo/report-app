package com.saas.report.tenant.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantEstadoTest {

    @Test
    void defineLosEstadosEsperados() {
        assertThat(TenantEstado.values()).containsExactly(
                TenantEstado.ACTIVE, TenantEstado.SUSPENDED, TenantEstado.CANCELED);
    }
}