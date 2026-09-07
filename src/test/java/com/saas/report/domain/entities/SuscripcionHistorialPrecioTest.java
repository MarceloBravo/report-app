package com.saas.report.domain.entities;

import com.saas.report.domain.enums.SuscripcionEstado;
import com.saas.report.shared.exception.InvalidAmountException;
import com.saas.report.domain.valueObjects.SuscripcionHistorialPrecioUUID;
import com.saas.report.domain.valueObjects.SuscripcionUUID;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuscripcionHistorialPrecioTest {

    private Suscripcion suscripcion() {
        TenantUUID tenant = TenantUUID.generate();
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("9.90"));
        Plan plan = new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));
        return new Suscripcion(SuscripcionUUID.generate(), tenant, plan, LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    private SuscripcionHistorialPrecio conTramo(Suscripcion s, LocalDate inicio, LocalDate fin, BigDecimal precio) {
        return new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(), s,
                inicio, fin, precio, s.getPlan().getPrecios().get(0));
    }

    @Test
    void construyeHistorialYExponeDatos() {
        Suscripcion s = suscripcion();
        SuscripcionHistorialPrecioUUID id = SuscripcionHistorialPrecioUUID.generate();
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();
        PlanPrecio tramo = s.getPlan().getPrecios().get(0);
        SuscripcionHistorialPrecio historial = new SuscripcionHistorialPrecio(id, s, inicio, fin,
                new BigDecimal("9.90"), tramo);

        assertThat(historial.getHistorialId()).isEqualTo(id);
        assertThat(historial.getSuscripcion()).isEqualTo(s);
        assertThat(historial.getPeriodoInicio()).isEqualTo(inicio);
        assertThat(historial.getPeriodoFin()).isEqualTo(fin);
        assertThat(historial.getPrecioAplicado()).isEqualByComparingTo("9.90");
        assertThat(historial.getPlanPrecio()).isEqualTo(tramo);
    }

    @Test
    void rechazaDatosInvalidos() {
        Suscripcion s = suscripcion();
        PlanPrecio tramo = s.getPlan().getPrecios().get(0);
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(null, s, hoy, null, BigDecimal.ONE, tramo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                null, hoy, null, BigDecimal.ONE, tramo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, null, null, BigDecimal.ONE, tramo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, hoy.minusDays(1), BigDecimal.ONE, tramo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, null, null, tramo))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID.generate(),
                s, hoy, null, new BigDecimal("-1"), tramo))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void estaVigenteSiLaFechaDeHoyCaeEnElPeriodo() {
        LocalDate hoy = LocalDate.now();

        assertThat(conTramo(suscripcion(), hoy.minusDays(10), null, BigDecimal.ONE).estaVigente()).isTrue();
        assertThat(conTramo(suscripcion(), hoy.minusDays(10), hoy.plusDays(5), BigDecimal.ONE).estaVigente()).isTrue();
        assertThat(conTramo(suscripcion(), hoy.minusDays(10), hoy.minusDays(1), BigDecimal.ONE).estaVigente()).isFalse();
        assertThat(conTramo(suscripcion(), hoy.plusDays(5), null, BigDecimal.ONE).estaVigente()).isFalse();
    }

    @Test
    void duracionDiasEsInclusiva() {
        LocalDate hoy = LocalDate.now();

        assertThat(conTramo(suscripcion(), hoy, hoy, BigDecimal.ONE).getDuracionDias()).isEqualTo(1L);
        assertThat(conTramo(suscripcion(), hoy.minusDays(4), hoy, BigDecimal.ONE).getDuracionDias()).isEqualTo(5L);
        assertThat(conTramo(suscripcion(), hoy.minusDays(2), null, BigDecimal.ONE).getDuracionDias()).isEqualTo(3L);
    }

    @Test
    void calculaElPrecioDiario() {
        LocalDate hoy = LocalDate.now();

        SuscripcionHistorialPrecio diezDias = conTramo(suscripcion(), hoy.minusDays(9), hoy, new BigDecimal("100"));
        SuscripcionHistorialPrecio periodoAbierto = conTramo(suscripcion(), hoy.minusDays(4), null, new BigDecimal("100"));

        assertThat(diezDias.getPrecioDiario()).isEqualByComparingTo("10");
        assertThat(periodoAbierto.getPrecioDiario()).isEqualByComparingTo("20");
    }
}