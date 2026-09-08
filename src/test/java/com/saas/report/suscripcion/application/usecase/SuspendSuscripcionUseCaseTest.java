package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspendSuscripcionUseCaseTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    private SuspendSuscripcionUseCase suspendSuscripcionUseCase;

    @BeforeEach
    void setUp() {
        suspendSuscripcionUseCase = new SuspendSuscripcionUseCase(suscripcionRepository);
    }

    private Suscripcion suscripcionActiva() {
        return new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), PlanUUID.generate(),
                LocalDate.now(), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    @Test
    void suspendeYCambiaElEstado() {
        Suscripcion suscripcion = suscripcionActiva();

        when(suscripcionRepository.findById(any(SuscripcionUUID.class))).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuscripcionResponse response = suspendSuscripcionUseCase.suspender(suscripcion.getSuscripcionId());

        assertThat(suscripcion.getEstado()).isEqualTo(SuscripcionEstado.SUSPENDED);
        assertThat(response.estado()).isEqualTo(SuscripcionEstado.SUSPENDED);
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(suscripcionRepository.findById(any(SuscripcionUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suspendSuscripcionUseCase.suspender(SuscripcionUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}