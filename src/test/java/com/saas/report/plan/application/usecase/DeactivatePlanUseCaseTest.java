package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivatePlanUseCaseTest {

    private static final String FREE_PLAN_NAME = "Starter";

    @Mock
    private PlanRepository planRepository;

    private DeactivatePlanUseCase deactivatePlanUseCase;

    @BeforeEach
    void setUp() {
        deactivatePlanUseCase = new DeactivatePlanUseCase(planRepository, FREE_PLAN_NAME);
    }

    private Plan plan(String nombre, boolean activo) {
        PlanUUID planId = PlanUUID.generate();
        return new Plan(planId, nombre, "Plan de prueba",
                new PlanMaxUsuarios(10), new PlanLimiteConsultasMensuales(100), activo,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("10.00"))));
    }

    @Test
    void desactivaElPlan() {
        Plan plan = plan("PRO", true);

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanResponse response = deactivatePlanUseCase.desactivar(plan.getPlanId());

        assertThat(plan.isActivo()).isFalse();
        assertThat(response.activo()).isFalse();
    }

    @Test
    void conservaLosTramosDePrecioAlDesactivar() {
        Plan plan = plan("PRO", true);

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        deactivatePlanUseCase.desactivar(plan.getPlanId());

        assertThat(plan.getPrecios()).hasSize(1);
        assertThat(plan.getPrecios().get(0).getPrecio()).isEqualByComparingTo("10.00");
    }

    @Test
    void lanzaExcepcionSiElPlanNoExiste() {
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deactivatePlanUseCase.desactivar(PlanUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lanzaExcepcionSiElPlanYaEstaInactivo() {
        Plan plan = plan("PRO", false);

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> deactivatePlanUseCase.desactivar(plan.getPlanId()))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void rechazaDesactivarElPlanGratuitoDelOnboarding() {
        Plan plan = plan(FREE_PLAN_NAME.toLowerCase(), true);

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> deactivatePlanUseCase.desactivar(plan.getPlanId()))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("onboarding");
        assertThat(plan.isActivo()).isTrue();
    }
}