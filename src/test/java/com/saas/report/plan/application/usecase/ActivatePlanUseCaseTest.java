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
class ActivatePlanUseCaseTest {

    @Mock
    private PlanRepository planRepository;

    private ActivatePlanUseCase activatePlanUseCase;

    @BeforeEach
    void setUp() {
        activatePlanUseCase = new ActivatePlanUseCase(planRepository);
    }

    private Plan planInactivo() {
        PlanUUID planId = PlanUUID.generate();
        return new Plan(planId, "PRO", "Plan pro",
                new PlanMaxUsuarios(20), new PlanLimiteConsultasMensuales(1000), false,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("10.00"))));
    }

    @Test
    void reactivaElPlan() {
        Plan plan = planInactivo();

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanResponse response = activatePlanUseCase.activar(plan.getPlanId());

        assertThat(plan.isActivo()).isTrue();
        assertThat(response.activo()).isTrue();
    }

    @Test
    void lanzaExcepcionSiElPlanNoExiste() {
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activatePlanUseCase.activar(PlanUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lanzaExcepcionSiElPlanYaEstaActivo() {
        PlanUUID planId = PlanUUID.generate();
        Plan plan = new Plan(planId, "PRO", "Plan pro",
                new PlanMaxUsuarios(20), new PlanLimiteConsultasMensuales(1000), true,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("10.00"))));

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> activatePlanUseCase.activar(planId))
                .isInstanceOf(InvalidStatusException.class);
    }
}