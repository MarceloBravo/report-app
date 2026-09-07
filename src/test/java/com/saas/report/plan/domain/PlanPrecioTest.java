package com.saas.report.plan.domain;

import com.saas.report.shared.exception.InvalidAmountException;
import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanPrecioTest {

    private PlanPrecio tramo(Integer mesDesde, Integer mesHasta, BigDecimal precio) {
        return new PlanPrecio(PlanPrecioUUID.generate(), PlanUUID.generate(), mesDesde, mesHasta, precio);
    }

    @Test
    void construyeTramoYExponeDatos() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecioUUID id = PlanPrecioUUID.generate();
        PlanPrecio tramo = new PlanPrecio(id, planId, 1, null, new BigDecimal("29.90"));

        assertThat(tramo.getPlanPrecioId()).isEqualTo(id);
        assertThat(tramo.getPlanId()).isEqualTo(planId);
        assertThat(tramo.getMesDesde()).isEqualTo(1);
        assertThat(tramo.getMesHasta()).isNull();
        assertThat(tramo.getPrecio()).isEqualByComparingTo("29.90");
    }

    @Test
    void rechazaDatosInvalidos() {
        PlanUUID planId = PlanUUID.generate();

        assertThatThrownBy(() -> new PlanPrecio(null, planId, 1, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), null, 1, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), planId, null, null, BigDecimal.ONE))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), planId, 0, null, BigDecimal.ONE))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), planId, 2, 1, BigDecimal.ONE))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, null))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("-1")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void permitePrecioCeroParaPlanesGratuitos() {
        assertThat(tramo(1, null, BigDecimal.ZERO).getPrecio()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void aplicaParaMesDentroDelTramo() {
        PlanPrecio tramo = tramo(1, 3, BigDecimal.TEN);

        assertThat(tramo.aplicaParaMes(1)).isTrue();
        assertThat(tramo.aplicaParaMes(3)).isTrue();
        assertThat(tramo.aplicaParaMes(0)).isFalse();
        assertThat(tramo.aplicaParaMes(4)).isFalse();
        assertThat(tramo.aplicaParaMes(null)).isFalse();
    }

    @Test
    void tramoFinalAplicaParaCualquierMes() {
        PlanPrecio tramo = tramo(1, null, BigDecimal.TEN);

        assertThat(tramo.aplicaParaMes(1)).isTrue();
        assertThat(tramo.aplicaParaMes(999_999)).isTrue();
        assertThat(tramo.esTramoFinal()).isTrue();
    }

    @Test
    void tramoConFinNoEsFinal() {
        assertThat(tramo(1, 3, BigDecimal.TEN).esTramoFinal()).isFalse();
    }

    @Test
    void igualdadPorValorDelTramo() {
        PlanPrecio a = tramo(1, 3, new BigDecimal("10.00"));
        PlanPrecio b = tramo(1, 3, new BigDecimal("10.00"));
        PlanPrecio c = tramo(1, 3, new BigDecimal("9.00"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }
}