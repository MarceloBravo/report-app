package com.saas.report.auth.domain.exception;

import com.saas.report.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionsTest {

    @Test
    void lasExcepcionesDeAutenticacionFormanLaJerarquiaEsperada() {
        assertThat(InvalidCredentialsException.class).isAssignableTo(DomainException.class);
        assertThat(TokenRevokedException.class).isAssignableTo(DomainException.class);
        assertThat(EmailAlreadyExistsException.class).isAssignableTo(DomainException.class);
    }

    @Test
    void preservanElMensaje() {
        assertThat(new InvalidCredentialsException("credenciales inválidas").getMessage()).isEqualTo("credenciales inválidas");
        assertThat(new TokenRevokedException("token revocado").getMessage()).isEqualTo("token revocado");
        assertThat(new EmailAlreadyExistsException("email duplicado").getMessage()).isEqualTo("email duplicado");
    }
}