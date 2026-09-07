package com.saas.report.domain.valueObjects;

import com.saas.report.domain.exceptions.InvalidIntegerValue;
import com.saas.report.domain.exceptions.InvalidUUIDException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void idsDeDominioRechazanNulo() {
        assertThatThrownBy(() -> new TenantUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new PlanUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new PlanPrecioUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new SuscripcionUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecioUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new TenantDbConnectionUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new ConsultaHistorialUUID(null)).isInstanceOf(InvalidUUIDException.class);
    }

    @Test
    void idsDeDominioExponenSuValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(new TenantUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new PlanUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new PlanPrecioUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new SuscripcionUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new SuscripcionHistorialPrecioUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new TenantDbConnectionUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new ConsultaHistorialUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void generateProduceValoresDistintos() {
        assertThat(TenantUUID.generate().getValue()).isNotNull();
        assertThat(TenantUUID.generate()).isNotEqualTo(TenantUUID.generate());
        assertThat(PlanUUID.generate()).isNotEqualTo(PlanUUID.generate());
        assertThat(PlanPrecioUUID.generate()).isNotEqualTo(PlanPrecioUUID.generate());
        assertThat(SuscripcionUUID.generate()).isNotEqualTo(SuscripcionUUID.generate());
        assertThat(SuscripcionHistorialPrecioUUID.generate()).isNotEqualTo(SuscripcionHistorialPrecioUUID.generate());
        assertThat(TenantDbConnectionUUID.generate()).isNotEqualTo(TenantDbConnectionUUID.generate());
        assertThat(ConsultaHistorialUUID.generate()).isNotEqualTo(ConsultaHistorialUUID.generate());
    }

    @Test
    void losIdsDeUnMismoValorSonIguales() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertThat(new PlanUUID(uuid)).isEqualTo(new PlanUUID(uuid));
        assertThat(new PlanUUID(uuid).hashCode()).isEqualTo(new PlanUUID(uuid).hashCode());
        assertThat(new TenantUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void planMaxUsuariosAceptaValoresValidos() {
        assertThat(new PlanMaxUsuarios(5).getValue()).isEqualTo(5);
        assertThat(new PlanMaxUsuarios(0).getValue()).isZero();
    }

    @Test
    void planMaxUsuariosRechazaValoresInvalidos() {
        assertThatThrownBy(() -> new PlanMaxUsuarios(null)).isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new PlanMaxUsuarios(-1)).isInstanceOf(InvalidIntegerValue.class);
    }

    @Test
    void limiteConsultasAceptaValoresValidos() {
        assertThat(new PlanLimiteConsultasMensuales(0).getValue()).isZero();
        assertThat(new PlanLimiteConsultasMensuales(100).getValue()).isEqualTo(100);
    }

    @Test
    void limiteConsultasIlimitadoUsaMenosUno() {
        PlanLimiteConsultasMensuales ilimitado = PlanLimiteConsultasMensuales.ilimitado();

        assertThat(ilimitado.getValue()).isEqualTo(-1);
        assertThat(ilimitado.esIlimitado()).isTrue();
        assertThat(new PlanLimiteConsultasMensuales(100).esIlimitado()).isFalse();
    }

    @Test
    void limiteConsultasRechazaValoresInvalidos() {
        assertThatThrownBy(() -> new PlanLimiteConsultasMensuales(null)).isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new PlanLimiteConsultasMensuales(-2)).isInstanceOf(InvalidIntegerValue.class);
    }
}