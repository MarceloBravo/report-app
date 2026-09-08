package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.ActualizarPlanRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActualizarPlanUseCaseTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    private ActualizarPlanUseCase actualizarPlanUseCase;

    @BeforeEach
    void setUp() {
        actualizarPlanUseCase = new ActualizarPlanUseCase(suscripcionRepository, planRepository);
    }

    private Plan planActivo(BigDecimal precio) {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, precio);
        return new Plan(planId, "PRO", "Plan pro", new PlanMaxUsuarios(20),
                new PlanLimiteConsultasMensuales(1000), true, List.of(tramo));
    }

    private Suscripcion suscripcionActiva() {
        return new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), PlanUUID.generate(),
                LocalDate.now(), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    @Test
    void actualizaElPlanDeLaSuscripcion() {
        Suscripcion suscripcion = suscripcionActiva();
        Plan nuevoPlan = planActivo(new BigDecimal("19.90"));

        when(suscripcionRepository.findById(any(SuscripcionUUID.class))).thenReturn(Optional.of(suscripcion));
        when(planRepository.findById(nuevoPlan.getPlanId())).thenReturn(Optional.of(nuevoPlan));
        when(suscripcionRepository.save(any(Suscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SuscripcionResponse response = actualizarPlanUseCase.actualizarPlan(
                suscripcion.getSuscripcionId(), new ActualizarPlanRequest(nuevoPlan.getPlanId().getValue()));

        assertThat(suscripcion.getPlanId()).isEqualTo(nuevoPlan.getPlanId());
        assertThat(response.planId()).isEqualTo(nuevoPlan.getPlanId().getValue());
    }

    @Test
    void lanzaExcepcionSiLaSuscripcionNoExiste() {
        when(suscripcionRepository.findById(any(SuscripcionUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actualizarPlanUseCase.actualizarPlan(
                SuscripcionUUID.generate(), new ActualizarPlanRequest(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lanzaExcepcionSiElPlanNoExiste() {
        Suscripcion suscripcion = suscripcionActiva();
        UUID planId = UUID.randomUUID();

        when(suscripcionRepository.findById(any(SuscripcionUUID.class))).thenReturn(Optional.of(suscripcion));
        when(planRepository.findById(new PlanUUID(planId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actualizarPlanUseCase.actualizarPlan(
                suscripcion.getSuscripcionId(), new ActualizarPlanRequest(planId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}