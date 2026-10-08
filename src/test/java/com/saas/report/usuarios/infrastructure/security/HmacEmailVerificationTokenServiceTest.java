package com.saas.report.usuarios.infrastructure.security;

import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacEmailVerificationTokenServiceTest {

    private HmacEmailVerificationTokenService service;

    @BeforeEach
    void setUp() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        service = new HmacEmailVerificationTokenService(key, 600_000);
    }

    @Test
    void generaYVerificaUnTokenValido() {
        UsuarioUUID usuarioId = UsuarioUUID.generate();

        String token = service.generar(usuarioId, "Juan@Correo.com");

        assertThat(service.verificar(token)).isEqualTo(usuarioId);
    }

    @Test
    void rechazaUnTokenModificado() {
        UsuarioUUID usuarioId = UsuarioUUID.generate();
        String token = service.generar(usuarioId, "juan@correo.com");
        String alterado = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> service.verificar(alterado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void rechazaUnTokenExpirado() {
        HmacEmailVerificationTokenService expirado =
                new HmacEmailVerificationTokenService(Base64.getEncoder().encodeToString(new byte[32]), -1_000);
        String token = expirado.generar(UsuarioUUID.generate(), "juan@correo.com");

        assertThatThrownBy(() -> service.verificar(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void rechazaUnFormatoInvalido() {
        assertThatThrownBy(() -> service.verificar("solo-un-segmento"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }
}