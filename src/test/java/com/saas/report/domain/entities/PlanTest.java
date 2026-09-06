package com.saas.report.domain.entities;

import com.saas.report.domain.valueObjects.PlanLimiteConsultasMensuales;
import com.saas.report.domain.valueObjects.PlanMaxUsuarios;
import com.saas.report.domain.valueObjects.PlanPrecioUUID;
import com.saas.report.domain.valueObjects.PlanUUID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanTest {

    private PlanPrecio tramoFijo(PlanUUID planId, BigDecimal precio) {
        return new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, precio);
    }

    private Plan plan(PlanLimiteConsultasMensuales limite) {
        PlanUUID planId = PlanUUID.generate();
        return new Plan(planId, "GROWTH", "Plan de crecimiento", new PlanMaxUsuarios(10),
                limite, true, List.of(tramoFijo(planId, new BigDecimal("29.90"))));
    }

    @Test
    void construyePlanYExponeDatos() {
        PlanUUID planId = PlanUUID.generate();
        Plan plan = new Plan(planId, "GROWTH", "Plan de crecimiento", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(500), true,
                List.of(tramoFijo(planId, new BigDecimal("29.90"))));

        assertThat(plan.getPlanId()).isEqualTo(planId);
        assertThat(plan.getNombrePlan()).isEqualTo("GROWTH");
        assertThat(plan.getDescripcion()).isEqualTo("Plan de crecimiento");
        assertThat(plan.getMaxUsuarios().getValue()).isEqualTo(10);
        assertThat(plan.getLimiteConsultasMensuales().getValue()).isEqualTo(500);
        assertThat(plan.isActivo()).isTrue();
        assertThat(plan.getPrecios()).hasSize(1);
    }

    @Test
    void rechazaAtributosInvalidos() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = tramoFijo(planId, new BigDecimal("29.90"));

        assertThatThrownBy(() -> new Plan(null, "GROWTH", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Plan(planId, "  ", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Plan(planId, "G", null, new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Plan(planId, "G", "d", null,
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Plan(planId, "G", "d", new PlanMaxUsuarios(10),
                null, true, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Plan(planId, "G", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), null, List.of(tramo)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaPlanSinTramos() {
        PlanUUID planId = PlanUUID.generate();

        assertThatThrownBy(() -> new Plan(planId, "G", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tramo");
        assertThatThrownBy(() -> new Plan(planId, "G", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaTramosDeOtroPlan() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramoDeOtroPlan = tramoFijo(PlanUUID.generate(), new BigDecimal("10.00"));

        assertThatThrownBy(() -> new Plan(planId, "G", "d", new PlanMaxUsuarios(10),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramoDeOtroPlan)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mismo plan");
    }

    @Test
    void esIlimitadoCuandoNoHayTopeDeConsultas() {
        assertThat(plan(PlanLimiteConsultasMensuales.ilimitado()).esIlimitado()).isTrue();
    }

    @Test
    void noEsIlimitadoConTopeDefinido() {
        assertThat(plan(new PlanLimiteConsultasMensuales(100)).esIlimitado()).isFalse();
    }

    @Test
    void permiteUsuariosHastaElMaximo() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        assertThat(plan.permiteUsuarios(10)).isTrue();
        assertThat(plan.permiteUsuarios(11)).isFalse();
        assertThat(plan.permiteUsuarios(0)).isTrue();
        assertThat(plan.permiteUsuarios(null)).isFalse();
        assertThat(plan.permiteUsuarios(-1)).isFalse();
    }

    @Test
    void permiteConsultasSegunElLimite() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        assertThat(plan.permiteConsultas(100)).isTrue();
        assertThat(plan.permiteConsultas(101)).isFalse();
        assertThat(plan.permiteConsultas(null)).isFalse();
        assertThat(plan.permiteConsultas(-1)).isFalse();
    }

    @Test
    void permiteCualquierCantidadDeConsultasSinLimite() {
        Plan plan = plan(PlanLimiteConsultasMensuales.ilimitado());

        assertThat(plan.permiteConsultas(0)).isTrue();
        assertThat(plan.permiteConsultas(1)).isTrue();
        assertThat(plan.permiteConsultas(1_000_000)).isTrue();
    }

    @Test
    void normalizaMesCeroAlPrimerMesAlBuscarPrecio() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        assertThat(plan.getPrecioParaMes(0)).isEqualTo(plan.getPrecioParaMes(1));
    }

    @Test
    void obtieneElMismoTramoParaCualquierMesConTramoFinal() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        PlanPrecio tramo1 = plan.getPrecioParaMes(1);
        PlanPrecio tramo2 = plan.getPrecioParaMes(500);

        assertThat(tramo1).isNotNull();
        assertThat(tramo2).isSameAs(tramo1);
    }

    @Test
    void rechazaMesNuloEnPrecio() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        assertThatThrownBy(() -> plan.getPrecioParaMes(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resuelveElTramoCorrectoSegunElMes() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio primerTramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, 3, new BigDecimal("10.00"));
        PlanPrecio tramoFinal = new PlanPrecio(PlanPrecioUUID.generate(), planId, 4, null, new BigDecimal("5.00"));
        Plan plan = new Plan(planId, "G", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(primerTramo, tramoFinal));

        assertThat(plan.getPrecioParaMes(1)).isEqualTo(primerTramo);
        assertThat(plan.getPrecioParaMes(3)).isEqualTo(primerTramo);
        assertThat(plan.getPrecioParaMes(4)).isEqualTo(tramoFinal);
        assertThat(plan.getPrecioParaMes(99)).isEqualTo(tramoFinal);
    }

    @Test
    void lanzaErrorCuandoNingunTramoCubreElMes() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, 3, new BigDecimal("10.00"));
        Plan plan = new Plan(planId, "G", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));

        assertThatThrownBy(() -> plan.getPrecioParaMes(4)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laListaDePreciosEsInmutable() {
        Plan plan = plan(new PlanLimiteConsultasMensuales(100));

        assertThatThrownBy(() -> plan.getPrecios().add(tramoFijo(plan.getPlanId(), new BigDecimal("1.00"))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void copiaDefensivaDePrecios() {
        PlanUUID planId = PlanUUID.generate();
        List<PlanPrecio> tramos = new ArrayList<>();
        tramos.add(tramoFijo(planId, new BigDecimal("10.00")));

        Plan plan = new Plan(planId, "G", "d", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(100), true, tramos);

        tramos.clear();

        assertThat(plan.getPrecios()).hasSize(1);
    }
}