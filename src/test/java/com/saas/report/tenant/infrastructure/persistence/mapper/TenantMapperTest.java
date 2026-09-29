package com.saas.report.tenant.infrastructure.persistence.mapper;

import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantMapperTest {

    private TenantJpaEntity entity() {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setTenantId(UUID.randomUUID());
        entity.setNombreEmpresa("Empresa Alpha");
        entity.setSubdominioSlug("empresa-alpha");
        entity.setEstado(TenantEstado.ACTIVE);
        entity.setFechaRegistro(LocalDateTime.now().minusDays(5));
        return entity;
    }

    @Test
    void convierteUnTenantAJpaEntity() {
        UUID id = UUID.randomUUID();
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(5);
        Tenant tenant = new Tenant(new TenantUUID(id), "Empresa Alpha", "empresa-alpha",
                TenantEstado.SUSPENDED, fechaRegistro);

        TenantJpaEntity entity = TenantMapper.toJpa(tenant);

        assertThat(entity.getTenantId()).isEqualTo(id);
        assertThat(entity.getNombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(entity.getSubdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(entity.getEstado()).isEqualTo(TenantEstado.SUSPENDED);
        assertThat(entity.getFechaRegistro()).isEqualTo(fechaRegistro);
    }

    @Test
    void convierteUnaJpaEntityATenant() {
        TenantJpaEntity entity = entity();

        Tenant tenant = TenantMapper.toDomain(entity);

        assertThat(tenant.getTenantId().getValue()).isEqualTo(entity.getTenantId());
        assertThat(tenant.getNombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(tenant.getSubdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(tenant.getFechaRegistro()).isEqualTo(entity.getFechaRegistro());
    }

    @Test
    void elViajeDeIdaYVueltaConservaTodosLosCampos() {
        TenantJpaEntity original = entity();

        TenantJpaEntity resultado = TenantMapper.toJpa(TenantMapper.toDomain(original));

        assertThat(resultado).usingRecursiveComparison().isEqualTo(original);
    }
}
