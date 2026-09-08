package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.SuscripcionRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.exception.SuscripcionAlreadyActiveException;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSuscripcionUseCaseTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    private CreateSuscripcionUseCase createSuscripcionUseCase;

    @BeforeEach
    void setUp() {
        createSuscripcionUseCase = new CreateSuscripcionUseCase(suscripcionRepository, planRepository);
    }

    private Plan planActivo(BigDecimal precio) {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, precio);
        return new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));
    }

    private SuscripcionRequest request(UUID planId) {
        return new SuscripcionRequest(UUID.randomUUID(), planId, null);
    }

    @Test
    void creaSuscripcionConHistorialDePrecioInicial() {
        Plan plan = planActivo(new BigDecimal("9.90"));
        SuscripcionRequest request = request(plan.getPlanId().getValue());

        when(suscripcionRepository.findByTenantIdAndEstado(any(), any())).thenReturn(Optional.empty());
        when(planRepository.findById(plan.getPlanId())).thenReturn(Optional.of(plan));
        when(suscripcionRepository.save(any(Suscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuscripcionResponse response = createSuscripcionUseCase.crear(request);

        ArgumentCaptor<Suscripcion> captor = ArgumentCaptor.forClass(Suscripcion.class);
        verify(suscripcionRepository).save(captor.capture());
        Suscripcion guardada = captor.getValue();

        assertThat(guardada.getPlanId()).isEqualTo(plan.getPlanId());
        assertThat(guardada.getEstado()).isEqualTo(SuscripcionEstado.ACTIVE);
        assertThat(guardada.getProximaFacturacion()).isEqualTo(guardada.getFechaInicio().plusMonths(1));
        assertThat(guardada.getHistorialPrecios()).hasSize(1);
        assertThat(guardada.getHistorialPrecios().get(0).getPrecioAplicado())
                .isEqualByComparingTo(new BigDecimal("9.90"));
        assertThat(guardada.getHistorialPrecios().get(0).getPeriodoFin()).isNull();

        assertThat(response.estado()).isEqualTo(SuscripcionEstado.ACTIVE);
        assertThat(response.historialPrecios()).hasSize(1);
    }

    @Test
    void rechazaCreacionSiElTenantYaTieneSuscripcionActiva() {
        Plan plan = planActivo(new BigDecimal("9.90"));
        SuscripcionRequest request = request(plan.getPlanId().getValue());
        Suscripcion activa = new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), plan.getPlanId(),
                LocalDate.now(), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));

        when(suscripcionRepository.findByTenantIdAndEstado(any(), any()))
                .thenReturn(Optional.of(activa));

        assertThatThrownBy(() -> createSuscripcionUseCase.crear(request))
                .isInstanceOf(SuscripcionAlreadyActiveException.class);
    }

    @Test
    void rechazaCreacionSiElPlanNoExiste() {
        UUID planId = UUID.randomUUID();
        SuscripcionRequest request = request(planId);

        when(suscripcionRepository.findByTenantIdAndEstado(any(), any())).thenReturn(Optional.empty());
        when(planRepository.findById(new PlanUUID(planId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createSuscripcionUseCase.crear(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaCreacionSiElPlanNoEstaActivo() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("5.00"));
        Plan planInactivo = new Plan(planId, "RETIRED", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), false, List.of(tramo));
        SuscripcionRequest request = request(planId.getValue());

        when(suscripcionRepository.findByTenantIdAndEstado(any(), any())).thenReturn(Optional.empty());
        when(planRepository.findById(planId)).thenReturn(Optional.of(planInactivo));

        assertThatThrownBy(() -> createSuscripcionUseCase.crear(request))
                .isInstanceOf(InvalidStatusException.class);
    }
}