package com.saas.report.tenant.infrastructure.persistence.adapter;

import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaEntity;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantJpaAdapterTest {

    @Mock
    private TenantJpaRepository repository;

    private TenantJpaAdapter tenantJpaAdapter;

    @BeforeEach
    void setUp() {
        tenantJpaAdapter = new TenantJpaAdapter(repository);
    }

    private TenantJpaEntity entity(UUID id, String nombreEmpresa, String subdominioSlug, TenantEstado estado) {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setTenantId(id);
        entity.setNombreEmpresa(nombreEmpresa);
        entity.setSubdominioSlug(subdominioSlug);
        entity.setEstado(estado);
        entity.setFechaRegistro(LocalDateTime.now().minusDays(5));
        return entity;
    }

    private Pageable pageableCapturado() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(nullable(String.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void mapeaLasEntidadesJpaAlDominio() {
        UUID id = UUID.randomUUID();
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(5);
        TenantJpaEntity entity = entity(id, "Empresa Alpha", "empresa-alpha", TenantEstado.ACTIVE);
        entity.setFechaRegistro(fechaRegistro);
        when(repository.findAll(eq((String) null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        Page<Tenant> result = tenantJpaAdapter.findAll(null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        Tenant tenant = result.getContent().get(0);
        assertThat(tenant.getTenantId().getValue()).isEqualTo(id);
        assertThat(tenant.getNombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(tenant.getSubdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(tenant.getFechaRegistro()).isEqualTo(fechaRegistro);
    }

    @Test
    void usaLaPaginaYSolicitados() {
        when(repository.findAll(eq("alpha"), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll("alpha", 2, 25);

        Pageable pageable = pageableCapturado();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void normalizaPageYNulosAPaginaCero() {
        when(repository.findAll(eq((String) null), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll(null, null, null);

        Pageable pageable = pageableCapturado();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void normalizaPaginaYTamanoNegativos() {
        when(repository.findAll(eq((String) null), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll(null, -5, -1);

        Pageable pageable = pageableCapturado();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void limitaElTamanoMaximoDePagina() {
        when(repository.findAll(eq((String) null), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll(null, 0, 5000);

        assertThat(pageableCapturado().getPageSize()).isEqualTo(200);
    }

    @Test
    void convierteUnSearchTextEnBlancoEnNull() {
        when(repository.findAll(eq((String) null), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll("   ", 0, 10);

        verify(repository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void recortaElSearchText() {
        when(repository.findAll(anyString(), any(Pageable.class))).thenReturn(Page.empty());

        tenantJpaAdapter.findAll("  empresa alpha  ", 0, 10);

        verify(repository).findAll(eq("empresa alpha"), any(Pageable.class));
    }

    @Test
    void devuelvePaginaVaciaCuandoNoHayTenants() {
        when(repository.findAll(eq((String) null), any(Pageable.class))).thenReturn(Page.empty());

        Page<Tenant> result = tenantJpaAdapter.findAll(null, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
