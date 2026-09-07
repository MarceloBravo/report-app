package com.saas.report.suscripcion.domain;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidAmountException;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionHistorialPrecioUUID;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuscripcionHistorialPrecioTest {

    private Plan planStarter() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("9.90"));
        return new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));
    }

    private Suscripcion suscripcion(Plan plan) {
        return new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), plan.getPlanId(), LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    private SuscripcionHistorialPrecio conTramo(Plan plan, LocalDate inicio, LocalDate fin, BigDecimal precio) {
        PlanPrecioUUID planPrecioId = plan.getPrecios().get(0).getPlanPrecioId();
        return new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(), suscripcion(plan),
                inicio, fin, precio, planPrecioId);
    }

    @Test
    void construyeHistorialYExponeDatos() {
        Plan plan = planStarter();
        Suscripcion s = suscripcion(plan);
        SuscripcionHistorialPrecioUUID id = SuscripcionHistorialPrecioUUID.generate();
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();
        PlanPrecioUUID tramoId = plan.getPrecios().get(0).getPlanPrecioId();
        SuscripcionHistorialPrecio historial = new SuscripcionHistorialPrecio(id, s, inicio, fin,
                new BigDecimal("9.90"), tramoId);

        assertThat(historial.getHistorialId()).isEqualTo(id);
        assertThat(historial.getSuscripcion()).isEqualTo(s);
        assertThat(historial.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(historial.getPeriodoFin()).isEqualTo(fin);
        assertThat(historial.getPrecioAplicado()).isEqualByComparingTo("9.90");
        assertThat(historial.getPlanPrecioId()).isEqualTo(tramoId);
    }

    @Test
    void rechazaDatosInvalidos() {
        Plan plan = planStarter();
        Suscripcion s = suscripcion(plan);
        PlanPrecioUUID tramoId = plan.getPrecios().get(0).getPlanPrecioId();
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(null, s, hoy, null, BigDecimal.ONE, tramoId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                null, hoy, null, BigDecimal.ONE, tramoId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, null, null, BigDecimal.ONE, tramoId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, hoy.minusDays(1), BigDecimal.ONE, tramoId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, null, null, tramoId))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, null, new BigDecimal("-1"), tramoId))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, null, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void estaVigenteSiLaFechaDeHoyCaeEnElPeriodo() {
        LocalDate hoy = LocalDate.now();

        assertThat(conTramo(planStarter(), hoy.minusDays(10), null, BigDecimal.ONE).estaVigente()).isTrue();
        assertThat(conTramo(planStarter(), hoy.minusDays(10), hoy.plusDays(5), BigDecimal.ONE).estaVigente()).isTrue();
        assertThat(conTramo(planStarter(), hoy.minusDays(10), hoy.minusDays(1), BigDecimal.ONE).estaVigente()).isFalse();
        assertThat(conTramo(planStarter(), hoy.plusDays(5), null, BigDecimal.ONE).estaVigente()).isFalse();
    }

    @Test
    void duracionDiasEsInclusiva() {
        LocalDate hoy = LocalDate.now();

        assertThat(conTramo(planStarter(), hoy, hoy, BigDecimal.ONE).getDuracionDias()).isEqualTo(1L);
        assertThat(conTramo(planStarter(), hoy.minusDays(4), hoy, BigDecimal.ONE).getDuracionDias()).isEqualTo(5L);
        assertThat(conTramo(planStarter(), hoy.minusDays(2), null, BigDecimal.ONE).getDuracionDias()).isEqualTo(3L);
    }

    @Test
    void calculaElPrecioDiario() {
        LocalDate hoy = LocalDate.now();

        SuscripcionHistorialPrecio diezDias = conTramo(planStarter(), hoy.minusDays(9), hoy, new BigDecimal("100"));
        SuscripcionHistorialPrecio periodoAbierto = conTramo(planStarter(), hoy.minusDays(4), null, new BigDecimal("100"));

        assertThat(diezDias.getPrecioDiario()).isEqualByComparingTo("10");
        assertThat(periodoAbierto.getPrecioDiario()).isEqualByComparingTo("20");
    }
}