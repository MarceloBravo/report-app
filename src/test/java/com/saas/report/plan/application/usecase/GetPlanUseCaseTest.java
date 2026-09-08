package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
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
class GetPlanUseCaseTest {

    @Mock
    private PlanRepository planRepository;

    private GetPlanUseCase getPlanUseCase;

    @BeforeEach
    void setUp() {
        getPlanUseCase = new GetPlanUseCase(planRepository);
    }

    private Plan plan() {
        PlanUUID planId = PlanUUID.generate();
        return new Plan(planId, "Plan Pro", "Para equipos en crecimiento",
                new PlanMaxUsuarios(20), new PlanLimiteConsultasMensuales(500), true,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, 6, new BigDecimal("49.00"))));
    }

    @Test
    void obtieneYExponeElPlan() {
        Plan plan = plan();

        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan));

        PlanResponse response = getPlanUseCase.obtener(plan.getPlanId());

        assertThat(response.planId()).isEqualTo(plan.getPlanId().getValue());
        assertThat(response.nombrePlan()).isEqualTo("Plan Pro");
        assertThat(response.maxUsuarios()).isEqualTo(20);
        assertThat(response.limiteConsultasMensuales()).isEqualTo(500);
        assertThat(response.activo()).isTrue();
        assertThat(response.precios()).hasSize(1);
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPlanUseCase.obtener(PlanUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}