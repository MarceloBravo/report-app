package com.saas.report.tenant.application.usecase;

import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTenantUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    private ListTenantUseCase listTenantUseCase;

    @BeforeEach
    void setUp() {
        listTenantUseCase = new ListTenantUseCase(tenantRepository);
    }

    private Tenant tenant(String nombreEmpresa, String subdominioSlug, TenantEstado estado) {
        return new Tenant(TenantUUID.generate(), nombreEmpresa, subdominioSlug, estado,
                LocalDateTime.now().minusDays(5));
    }

    @Test
    void mapeaCadaTenantDeLaPaginaAResponse() {
        Tenant primero = tenant("Empresa Alpha", "empresa-alpha", TenantEstado.ACTIVE);
        Tenant segundo = tenant("Empresa Beta", "empresa-beta", TenantEstado.SUSPENDED);
        when(tenantRepository.findAll("alpha", 0, 10))
                .thenReturn(new PageImpl<>(List.of(primero, segundo), PageRequest.of(0, 10), 2));

        Page<TenantResponse> response = listTenantUseCase.list("alpha", 0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).tenantId()).isEqualTo(primero.getTenantId().getValue());
        assertThat(response.getContent().get(0).nombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(response.getContent().get(0).subdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(response.getContent().get(0).estado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(response.getContent().get(0).fechaRegistro()).isEqualTo(primero.getFechaRegistro());
        assertThat(response.getContent().get(1).estado()).isEqualTo(TenantEstado.SUSPENDED);
    }

    @Test
    void conservaLosMetadatosDePaginacion() {
        Tenant tenant = tenant("Empresa Alpha", "empresa-alpha", TenantEstado.ACTIVE);
        when(tenantRepository.findAll(null, 0, 5))
                .thenReturn(new PageImpl<>(List.of(tenant), PageRequest.of(0, 5), 11));

        Page<TenantResponse> response = listTenantUseCase.list(null, 0, 5);

        assertThat(response.getTotalElements()).isEqualTo(11);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void devuelvePaginaVaciaSinTenants() {
        when(tenantRepository.findAll(null, 0, 10)).thenReturn(Page.empty());

        Page<TenantResponse> response = listTenantUseCase.list(null, 0, 10);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    void delegaSearchTextPageYSizeAlRepositorio() {
        when(tenantRepository.findAll("empresa", 3, 25)).thenReturn(Page.empty());

        listTenantUseCase.list("empresa", 3, 25);

        verify(tenantRepository).findAll(eq("empresa"), eq(3), eq(25));
    }
}
