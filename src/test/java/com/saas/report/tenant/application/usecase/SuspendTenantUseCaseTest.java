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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspendTenantUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    private SuspendTenantUseCase suspendTenantUseCase;

    @BeforeEach
    void setUp() {
        suspendTenantUseCase = new SuspendTenantUseCase(tenantRepository);
    }

    private Tenant tenant(TenantEstado estado) {
        return new Tenant(TenantUUID.generate(), "Empresa Alpha", "empresa-alpha",
                estado, LocalDateTime.now().minusDays(5));
    }

    @Test
    void suspendeUnTenantActivo() {
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantResponse response = suspendTenantUseCase.suspender(tenant.getTenantId());

        assertThat(tenant.getEstado()).isEqualTo(TenantEstado.SUSPENDED);
        assertThat(tenant.estaActivo()).isFalse();
        assertThat(response.estado()).isEqualTo(TenantEstado.SUSPENDED);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suspendTenantUseCase.suspender(TenantUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}