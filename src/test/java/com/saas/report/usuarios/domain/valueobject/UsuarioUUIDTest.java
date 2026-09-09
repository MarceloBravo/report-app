package com.saas.report.usuarios.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioUUIDTest {

    @Test
    void rechazaNulo() {
        assertThatThrownBy(() -> new UsuarioUUID(null)).isInstanceOf(InvalidUUIDException.class);
    }

    @Test
    void exponeSuValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(new UsuarioUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void generateProduceValoresDistintos() {
        assertThat(UsuarioUUID.generate()).isNotEqualTo(UsuarioUUID.generate());
    }

    @Test
    void losIdsDeUnMismoValorSonIguales() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertThat(new UsuarioUUID(uuid)).isEqualTo(new UsuarioUUID(uuid));
        assertThat(new UsuarioUUID(uuid).hashCode()).isEqualTo(new UsuarioUUID(uuid).hashCode());
    }
}