package com.saas.report.auth.domain.valueobject;

import com.saas.report.shared.exception.InvalidUUIDException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthValueObjectsTest {

    @Test
    void idsDeAutenticacionRechazanNulo() {
        assertThatThrownBy(() -> new UsuarioTokenUUID(null)).isInstanceOf(InvalidUUIDException.class);
    }

    @Test
    void idsDeAutenticacionExponenSuValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(new UsuarioTokenUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void generateProduceValoresDistintos() {
        assertThat(UsuarioTokenUUID.generate()).isNotEqualTo(UsuarioTokenUUID.generate());
    }

    @Test
    void losIdsDeUnMismoValorSonIguales() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertThat(new UsuarioTokenUUID(uuid)).isEqualTo(new UsuarioTokenUUID(uuid));
        assertThat(new UsuarioTokenUUID(uuid).hashCode()).isEqualTo(new UsuarioTokenUUID(uuid).hashCode());
    }
}