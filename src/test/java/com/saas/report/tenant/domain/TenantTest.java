package com.saas.report.tenant.domain;

import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantTest {

    private Tenant tenantActivo() {
        return new Tenant(TenantUUID.generate(), "Empresa Alpha", "empresa-alpha", TenantEstado.ACTIVE);
    }

    @Test
    void construyeTenantYExponeDatos() {
        TenantUUID id = TenantUUID.generate();
        Tenant tenant = new Tenant(id, "Empresa Alpha", "empresa-alpha", TenantEstado.ACTIVE);

        assertThat(tenant.getTenantId()).isEqualTo(id);
        assertThat(tenant.getNombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(tenant.getSubdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(tenant.getFechaRegistro()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(tenant.estaActivo()).isTrue();
    }

    @Test
    void suspenderCambiaElEstado() {
        Tenant tenant = tenantActivo();
        tenant.suspender();

        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.SUSPENDED);
        assertThat(tenant.estaActivo()).isFalse();
    }

    @Test
    void suspenderUnTenantYaSuspendidoLanzaExcepcion() {
        Tenant tenant = tenantActivo();
        tenant.suspender();

        assertThatThrownBy(tenant::suspender)
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("suspendido");
    }

    @Test
    void reactivarDevuelveAlEstadoActivo() {
        Tenant tenant = tenantActivo();
        tenant.suspender();
        tenant.reactivar();

        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(tenant.estaActivo()).isTrue();
    }

    @Test
    void reactivarUnTenantActivoLanzaExcepcion() {
        Tenant tenant = tenantActivo();

        assertThatThrownBy(tenant::reactivar)
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("activo");
    }

    @Test
    void cancelarCambiaElEstado() {
        Tenant tenant = tenantActivo();
        tenant.cancelar();

        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.CANCELED);
        assertThat(tenant.estaActivo()).isFalse();
    }

    @Test
    void cancelarUnTenantYaCanceladoLanzaExcepcion() {
        Tenant tenant = tenantActivo();
        tenant.cancelar();

        assertThatThrownBy(tenant::cancelar)
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("cancelado");
    }
}