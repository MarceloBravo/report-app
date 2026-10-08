package com.saas.report.tenant.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTenantUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    private GetTenantUseCase getTenantUseCase;

    @BeforeEach
    void setUp() {
        getTenantUseCase = new GetTenantUseCase(tenantRepository);
    }

    private Tenant tenant() {
        return new Tenant(TenantUUID.generate(), "Empresa Alpha", "empresa-alpha",
                TenantEstado.ACTIVE, LocalDateTime.now().minusDays(5));
    }

    @Test
    void obtieneYExponeElTenant() {
        Tenant tenant = tenant();

        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));

        TenantResponse response = getTenantUseCase.obtener(tenant.getTenantId());

        assertThat(response.tenantId()).isEqualTo(tenant.getTenantId().getValue());
        assertThat(response.nombreEmpresa()).isEqualTo("Empresa Alpha");
        assertThat(response.subdominioSlug()).isEqualTo("empresa-alpha");
        assertThat(response.estado()).isEqualTo(TenantEstado.ACTIVE);
        assertThat(response.fechaRegistro()).isEqualTo(tenant.getFechaRegistro());
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getTenantUseCase.obtener(TenantUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}