package com.saas.report.tenant.application.usecase;

import com.saas.report.tenant.application.dto.TenantRequest;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTenantUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    private CreateTenantUseCase createTenantUseCase;

    @BeforeEach
    void setUp() {
        createTenantUseCase = new CreateTenantUseCase(tenantRepository);
    }

    @Test
    void creaTenantActivo() {
        TenantRequest request = new TenantRequest("Empresa Alpha", "empresa-alpha");

        when(tenantRepository.existsBySubdominioSlug("empresa-alpha")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantResponse response = createTenantUseCase.crear(request);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant guardado = captor.getValue();
        assertThat(guardado.getNombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(guardado.getSubdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(guardado.getEstado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(guardado.estaActivo()).isTrue();

        assertThat(response.nombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(response.subdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(response.estado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(response.tenantId()).isEqualTo(guardado.getTenantId().getValue());
    }

    @Test
    void rechazaSubdominioYaExistente() {
        TenantRequest request = new TenantRequest("Empresa Alpha", "empresa-alpha");

        when(tenantRepository.existsBySubdominioSlug("empresa-alpha")).thenReturn(true);

        assertThatThrownBy(() -> createTenantUseCase.crear(request))
                .isInstanceOf(TenantSlugAlreadyExistsException.class);
    }
}