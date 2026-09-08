package com.saas.report.tenant.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelTenantUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    private CancelTenantUseCase cancelTenantUseCase;

    @BeforeEach
    void setUp() {
        cancelTenantUseCase = new CancelTenantUseCase(tenantRepository);
    }

    private Tenant tenant(TenantEstado estado) {
        return new Tenant(TenantUUID.generate(), "Empresa Alpha", "empresa-alpha",
                estado, LocalDateTime.now().minusDays(5));
    }

    @Test
    void cancelaUnTenantActivo() {
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantResponse response = cancelTenantUseCase.cancelar(tenant.getTenantId());

        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.CANCELED);
        assertThat(tenant.estaActivo()).isFalse();
        assertThat(response.estado()).isEqualTo(TenantEstado.CANCELED);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cancelTenantUseCase.cancelar(TenantUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}