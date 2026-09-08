package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPlansUseCaseTest {

    @Mock
    private PlanRepository planRepository;

    private ListPlansUseCase listPlansUseCase;

    @BeforeEach
    void setUp() {
        listPlansUseCase = new ListPlansUseCase(planRepository);
    }

    private Plan plan(String nombre, boolean ilimitado) {
        PlanUUID planId = PlanUUID.generate();
        PlanLimiteConsultasMensuales limite = ilimitado
                ? PlanLimiteConsultasMensuales.ilimitado()
                : new PlanLimiteConsultasMensuales(100);
        return new Plan(planId, nombre, "Descripción", new PlanMaxUsuarios(5), limite, true,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("29.00"))));
    }

    @Test
    void listaLosPlanes() {
        when(planRepository.findAll()).thenReturn(List.of(plan("Básico", false), plan("Premium", true)));

        List<PlanResponse> response = listPlansUseCase.listar();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).nombrePlan()).isEqualTo("Básico");
        assertThat(response.get(1).esIlimitado()).isTrue();
    }
}