package com.saas.report.domain.entities;

import com.saas.report.domain.enums.SuscripcionEstado;
import com.saas.report.shared.exception.InvalidStatusException;
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

class SuscripcionTest {

    private TenantUUID tenant() {
        return TenantUUID.generate();
    }

    private Plan planConTramoFijo() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("9.90"));
        return new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));
    }

    private Suscripcion suscripcionActiva() {
        return new Suscripcion(SuscripcionUUID.generate(), tenant(), planConTramoFijo(), LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    @Test
    void construyeSuscripcionYExponeDatos() {
        TenantUUID tenant = tenant();
        Plan plan = planConTramoFijo();
        SuscripcionUUID id = SuscripcionUUID.generate();
        LocalDate inicio = LocalDate.now();
        LocalDate proximaFacturacion = inicio.plusMonths(1);
        Suscripcion suscripcion = new Suscripcion(id, tenant, plan, inicio, SuscripcionEstado.ACTIVE, proximaFacturacion);

        assertThat(suscripcion.getSuscripcionId()).isEqualTo(id);
        assertThat(suscripcion.getTenantId()).isEqualTo(tenant);
        assertThat(suscripcion.getPlan()).isEqualTo(plan);
        assertThat(suscripcion.getFechaInicio()).isEqualTo(inicio);
        assertThat(suscripcion.getEstado()).isEqualTo(SuscripcionEstado.ACTIVE);
        assertThat(suscripcion.getProximaFacturacion()).isEqualTo(proximaFacturacion);
    }

    @Test
    void rechazaAtributosNulos() {
        TenantUUID tenant = tenant();
        Plan plan = planConTramoFijo();
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> new Suscripcion(null, tenant, plan, hoy, SuscripcionEstado.ACTIVE, hoy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Suscripcion(SuscripcionUUID.generate(), null, plan, hoy, SuscripcionEstado.ACTIVE, hoy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Suscripcion(SuscripcionUUID.generate(), tenant, null, hoy, SuscripcionEstado.ACTIVE, hoy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Suscripcion(SuscripcionUUID.generate(), tenant, plan, null, SuscripcionEstado.ACTIVE, hoy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Suscripcion(SuscripcionUUID.generate(), tenant, plan, hoy, null, hoy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cuentaLosMesesContratadosDesdeElInicio() {
        Suscripcion deTresMeses = new Suscripcion(SuscripcionUUID.generate(), tenant(), planConTramoFijo(),
                LocalDate.now().minusMonths(3), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
        Suscripcion recienCreada = new Suscripcion(SuscripcionUUID.generate(), tenant(), planConTramoFijo(),
                LocalDate.now(), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));

        assertThat(deTresMeses.getMesesContratados()).isEqualTo(3);
        assertThat(recienCreada.getMesesContratados()).isEqualTo(0);
    }

    @Test
    void estaVigenteSoloCuandoEstaActiva() {
        assertThat(suscripcionActiva().estaVigente()).isTrue();

        Suscripcion suspendida = suscripcionActiva();
        suspendida.suspender();
        assertThat(suspendida.estaVigente()).isFalse();
    }

    @Test
    void noRequiereCambioDeTramoConPrecioFijo() {
        Suscripcion suscripcion = new Suscripcion(SuscripcionUUID.generate(), tenant(), planConTramoFijo(),
                LocalDate.now().minusMonths(2), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));

        assertThat(suscripcion.requiereCambioDeTramo()).isFalse();
    }

    @Test
    void requiereCambioDeTramoAlAvanzarDeTramoDePrecio() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio primerTramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, 2, new BigDecimal("10.00"));
        PlanPrecio tramoFinal = new PlanPrecio(PlanPrecioUUID.generate(), planId, 3, null, new BigDecimal("5.00"));
        Plan plan = new Plan(planId, "G", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(primerTramo, tramoFinal));

        Suscripcion suscripcion = new Suscripcion(SuscripcionUUID.generate(), tenant(), plan,
                LocalDate.now().minusMonths(2), SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));

        assertThat(suscripcion.requiereCambioDeTramo()).isTrue();
    }

    @Test
    void noRequiereCambioDeTramoSiNoEstaVigente() {
        Suscripcion suspendida = suscripcionActiva();
        suspendida.suspender();

        assertThat(suspendida.requiereCambioDeTramo()).isFalse();
    }

    @Test
    void cancelarCambiaElEstado() {
        Suscripcion suscripcion = suscripcionActiva();
        suscripcion.cancelar();

        assertThat(suscripcion.getEstado()).isEqualTo(SuscripcionEstado.CANCELED);
        assertThat(suscripcion.estaVigente()).isFalse();
    }

    @Test
    void cancelarUnaSuscripcionYaCanceladaLanzaExcepcion() {
        Suscripcion suscripcion = suscripcionActiva();
        suscripcion.cancelar();

        assertThatThrownBy(suscripcion::cancelar)
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void suspenderCambiaElEstado() {
        Suscripcion suscripcion = suscripcionActiva();
        suscripcion.suspender();

        assertThat(suscripcion.getEstado()).isEqualTo(SuscripcionEstado.SUSPENDED);
    }

    @Test
    void suspenderUnaSuscripcionCanceladaLanzaExcepcion() {
        Suscripcion suscripcion = suscripcionActiva();
        suscripcion.cancelar();

        assertThatThrownBy(suscripcion::suspender)
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void actualizarPlanCambiaElPlan() {
        Suscripcion suscripcion = suscripcionActiva();
        Plan nuevoPlan = planConTramoFijo();

        suscripcion.actualizarPlan(nuevoPlan);

        assertThat(suscripcion.getPlan()).isEqualTo(nuevoPlan);
    }

    @Test
    void actualizarPlanConNuloLanzaExcepcion() {
        Suscripcion suscripcion = suscripcionActiva();

        assertThatThrownBy(() -> suscripcion.actualizarPlan(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarPlanInactivoLanzaExcepcion() {
        Suscripcion suscripcion = suscripcionActiva();
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("5.00"));
        Plan planInactivo = new Plan(planId, "RETIRED", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), false, List.of(tramo));

        assertThatThrownBy(() -> suscripcion.actualizarPlan(planInactivo))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void guardaHistorialDePreciosInmutable() {
        Suscripcion suscripcion = suscripcionActiva();
        PlanPrecio tramo = suscripcion.getPlan().getPrecios().get(0);
        SuscripcionHistorialPrecio historial = new SuscripcionHistorialPrecio(
                SuscripcionHistorialPrecioUUID.generate(), suscripcion, LocalDate.now(), null,
                new BigDecimal("9.90"), tramo);

        suscripcion.addHistorialPrecio(historial);

        assertThat(suscripcion.getHistorialPrecios()).containsExactly(historial);
        assertThat(suscripcion.getHistorialPrecios()).extracting(SuscripcionHistorialPrecio::getPeriodoInicio)
                .containsExactly(LocalDate.now());
        assertThatThrownBy(() -> suscripcion.addHistorialPrecio(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> suscripcion.getHistorialPrecios().add(historial))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}