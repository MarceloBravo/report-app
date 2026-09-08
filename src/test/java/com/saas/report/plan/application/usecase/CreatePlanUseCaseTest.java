package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanPrecioRequest;
import com.saas.report.plan.application.dto.PlanRequest;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.exception.PlanNombreAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePlanUseCaseTest {

    @Mock
    private PlanRepository planRepository;

    private CreatePlanUseCase createPlanUseCase;

    @BeforeEach
    void setUp() {
        createPlanUseCase = new CreatePlanUseCase(planRepository);
    }

    private PlanRequest request() {
        return new PlanRequest(
                "Plan Pro",
                "Para equipos en crecimiento",
                20,
                500,
                true,
                List.of(
                        new PlanPrecioRequest(1, 6, new BigDecimal("49.00")),
                        new PlanPrecioRequest(7, null, new BigDecimal("39.00"))));
    }

    @Test
    void creaPlanConSusTramosDePrecio() {
        PlanRequest request = request();

        when(planRepository.existsByNombrePlan("Plan Pro")).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanResponse response = createPlanUseCase.crear(request);

        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planRepository).save(captor.capture());
        Plan guardado = captor.getValue();
        assertThat(guardado.getNombrePlan()).isEqualTo("Plan Pro");
        assertThat(guardado.getMaxUsuarios().getValue()).isEqualTo(20);
        assertThat(guardado.getLimiteConsultasMensuales().getValue()).isEqualTo(500);
        assertThat(guardado.isActivo()).isTrue();
        assertThat(guardado.getPrecios()).hasSize(2);
        assertThat(guardado.getPrecios()).allMatch(precio -> precio.getPlanId().equals(guardado.getPlanId()));
        assertThat(guardado.getPrecios().get(0).getPrecio()).isEqualByComparingTo("49.00");

        assertThat(response.nombrePlan()).isEqualTo("Plan Pro");
        assertThat(response.precios()).hasSize(2);
        assertThat(response.esIlimitado()).isFalse();
    }

    @Test
    void rechazaNombreDePlanYaExistente() {
        PlanRequest request = request();

        when(planRepository.existsByNombrePlan("Plan Pro")).thenReturn(true);

        assertThatThrownBy(() -> createPlanUseCase.crear(request))
                .isInstanceOf(PlanNombreAlreadyExistsException.class);
    }
}