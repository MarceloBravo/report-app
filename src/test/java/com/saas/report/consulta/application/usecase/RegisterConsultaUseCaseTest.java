package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterConsultaUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    private RegisterConsultaUseCase registerConsultaUseCase;

    @BeforeEach
    void setUp() {
        registerConsultaUseCase = new RegisterConsultaUseCase(
                consultaRepository, suscripcionRepository, planRepository);
    }

    private ConsultaRequest request() {
        return new ConsultaRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "¿Cuántas ventas hubo en marzo?",
                "SELECT * FROM ventas;",
                true,
                42,
                null);
    }

    private Plan planConLimite(Integer limite) {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("9.90"));
        return new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(limite), true, List.of(tramo));
    }

    private Suscripcion suscripcionActiva(PlanUUID planId) {
        return new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), planId, LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    @Test
    void registraConsultaCalculandoElConsumoDelPeriodo() {
        ConsultaRequest request = request();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = registerConsultaUseCase.registrar(request);

        ArgumentCaptor<ConsultaHistorial> captor = ArgumentCaptor.forClass(ConsultaHistorial.class);
        verify(consultaRepository).save(captor.capture());
        ConsultaHistorial guardada = captor.getValue();
        assertThat(guardada.getTenantId().getValue()).isEqualTo(request.tenantId());
        assertThat(guardada.getPreguntaUsuario()).isEqualTo("¿Cuántas ventas hubo en marzo?");
        assertThat(guardada.getQuerySqlGenerada()).isEqualTo("SELECT * FROM ventas;");
        assertThat(guardada.getExitosa()).isTrue();
        assertThat(guardada.getRegistrosCount()).isEqualTo(42);
        assertThat(guardada.getConsultasEnPeriodo()).isEqualTo(5);

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
        assertThat(response.consultaId()).isEqualTo(guardada.getConsultaId().getValue());
        assertThat(response.fechaConsulta()).isNotNull();
    }

    @Test
    void dejaPasarCuandoLaSuscripcionEstaDentroDelLimite() {
        Plan plan = planConLimite(100);
        when(suscripcionRepository.findByTenantIdAndEstado(any(), any()))
                .thenReturn(Optional.of(suscripcionActiva(plan.getPlanId())));
        when(planRepository.findById(plan.getPlanId())).thenReturn(Optional.of(plan));
        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = registerConsultaUseCase.registrar(request());

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
    }

    @Test
    void rechazaCuandoSeExcedeElLimiteDelPlan() {
        Plan plan = planConLimite(5);
        when(suscripcionRepository.findByTenantIdAndEstado(any(), any()))
                .thenReturn(Optional.of(suscripcionActiva(plan.getPlanId())));
        when(planRepository.findById(plan.getPlanId())).thenReturn(Optional.of(plan));
        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(5L);

        assertThatThrownBy(() -> registerConsultaUseCase.registrar(request()))
                .isInstanceOf(LimiteConsultasExcedidoException.class)
                .hasMessageContaining("límite");
    }
}