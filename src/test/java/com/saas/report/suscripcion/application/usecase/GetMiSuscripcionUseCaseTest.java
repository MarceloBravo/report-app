package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMiSuscripcionUseCaseTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    private GetMiSuscripcionUseCase getMiSuscripcionUseCase;

    @BeforeEach
    void setUp() {
        getMiSuscripcionUseCase = new GetMiSuscripcionUseCase(suscripcionRepository, planRepository);
    }

    private Suscripcion suscripcion(TenantUUID tenantId, PlanUUID planId, SuscripcionEstado estado) {
        return new Suscripcion(SuscripcionUUID.generate(), tenantId, planId,
                LocalDate.now().minusMonths(1), estado, LocalDate.now().plusMonths(1));
    }

    private Plan plan(PlanUUID planId) {
        return new Plan(planId, "Plan Pro", "Para equipos en crecimiento",
                new PlanMaxUsuarios(20), new PlanLimiteConsultasMensuales(500), true,
                List.of(new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, 6, new BigDecimal("49.00"))));
    }

    @Test
    void devuelveLaSuscripcionDelTenantConLosDatosDelPlan() {
        TenantUUID tenantId = TenantUUID.generate();
        PlanUUID planId = PlanUUID.generate();
        Suscripcion suscripcion = suscripcion(tenantId, planId, SuscripcionEstado.ACTIVE);

        when(suscripcionRepository.findByTenantId(any(TenantUUID.class))).thenReturn(List.of(suscripcion));
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan(planId)));

        MiSuscripcionResponse response = getMiSuscripcionUseCase.obtenerMiSuscripcion(tenantId);

        assertThat(response.suscripcion().suscripcionId())
                .isEqualTo(suscripcion.getSuscripcionId().getValue());
        assertThat(response.suscripcion().tenantId()).isEqualTo(tenantId.getValue());
        assertThat(response.suscripcion().estado()).isEqualTo(SuscripcionEstado.ACTIVE);
        assertThat(response.plan().planId()).isEqualTo(planId.getValue());
        assertThat(response.plan().nombrePlan()).isEqualTo("Plan Pro");
        assertThat(response.plan().maxUsuarios()).isEqualTo(20);
        assertThat(response.plan().limiteConsultasMensuales()).isEqualTo(500);
        assertThat(response.plan().precios()).hasSize(1);
    }

    @Test
    void devuelveLaSuscripcionMasRecienteCuandoHayVarias() {
        TenantUUID tenantId = TenantUUID.generate();
        PlanUUID planId = PlanUUID.generate();
        Suscripcion reciente = suscripcion(tenantId, planId, SuscripcionEstado.ACTIVE);
        Suscripcion antigua = suscripcion(tenantId, planId, SuscripcionEstado.CANCELED);

        when(suscripcionRepository.findByTenantId(any(TenantUUID.class)))
                .thenReturn(List.of(reciente, antigua));
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.of(plan(planId)));

        MiSuscripcionResponse response = getMiSuscripcionUseCase.obtenerMiSuscripcion(tenantId);

        assertThat(response.suscripcion().suscripcionId())
                .isEqualTo(reciente.getSuscripcionId().getValue());
        assertThat(response.suscripcion().estado()).isEqualTo(SuscripcionEstado.ACTIVE);
    }

    @Test
    void lanzaExcepcionSiElTenantNoTieneSuscripcion() {
        when(suscripcionRepository.findByTenantId(any(TenantUUID.class))).thenReturn(List.of());

        assertThatThrownBy(() -> getMiSuscripcionUseCase.obtenerMiSuscripcion(TenantUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lanzaExcepcionSiElPlanDeLaSuscripcionNoExiste() {
        TenantUUID tenantId = TenantUUID.generate();
        when(suscripcionRepository.findByTenantId(any(TenantUUID.class)))
                .thenReturn(List.of(suscripcion(tenantId, PlanUUID.generate(), SuscripcionEstado.ACTIVE)));
        when(planRepository.findById(any(PlanUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getMiSuscripcionUseCase.obtenerMiSuscripcion(tenantId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}