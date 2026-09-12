package com.saas.report.tenant.infrastructure.schema;

import com.saas.report.tenant.domain.schema.ColumnaEsquema;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EsquemaCacheServiceTest {

    private EsquemaCacheService cache;

    @BeforeEach
    void setUp() {
        cache = new EsquemaCacheService();
    }

    private EsquemaTenant esquema(TenantUUID tenantId) {
        return new EsquemaTenant(tenantId, List.of(
                new TablaEsquema("clientes", List.of(
                        new ColumnaEsquema("id", "integer", false, true)))),
                Instant.now());
    }

    @Test
    void noEncuentraEsquemaSiNoExiste() {
        assertThat(cache.buscarPorTenant(TenantUUID.generate())).isEmpty();
    }

    @Test
    void guardaYRecuperaElEsquemaPorTenant() {
        TenantUUID tenantId = TenantUUID.generate();
        EsquemaTenant esquema = esquema(tenantId);

        cache.guardar(tenantId, esquema);

        Optional<EsquemaTenant> resultado = cache.buscarPorTenant(tenantId);
        assertThat(resultado).isPresent();
        assertThat(resultado.get().tenantId()).isEqualTo(tenantId);
        assertThat(resultado.get().tablas()).hasSize(1);
        assertThat(resultado.get().tablas().get(0).nombre()).isEqualTo("clientes");
    }

    @Test
    void invalidaElEsquemaDelTenant() {
        TenantUUID tenantId = TenantUUID.generate();
        cache.guardar(tenantId, esquema(tenantId));

        cache.invalidar(tenantId);

        assertThat(cache.buscarPorTenant(tenantId)).isEmpty();
    }
}