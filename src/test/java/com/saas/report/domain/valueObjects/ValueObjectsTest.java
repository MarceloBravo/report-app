package com.saas.report.domain.valueObjects;

import com.saas.report.shared.exception.InvalidUUIDException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void idsDeDominioRechazanNulo() {
        assertThatThrownBy(() -> new SuscripcionUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new SuscripcionHistorialPrecioUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new ConsultaHistorialUUID(null)).isInstanceOf(InvalidUUIDException.class);
    }

    @Test
    void idsDeDominioExponenSuValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(new SuscripcionUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new SuscripcionHistorialPrecioUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new ConsultaHistorialUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void generateProduceValoresDistintos() {
        assertThat(SuscripcionUUID.generate()).isNotEqualTo(SuscripcionUUID.generate());
        assertThat(SuscripcionHistorialPrecioUUID.generate()).isNotEqualTo(SuscripcionHistorialPrecioUUID.generate());
        assertThat(ConsultaHistorialUUID.generate()).isNotEqualTo(ConsultaHistorialUUID.generate());
    }
}