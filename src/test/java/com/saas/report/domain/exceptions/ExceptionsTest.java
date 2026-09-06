package com.saas.report.domain.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void lasExcepcionesFormanLaJerarquiaEsperada() {
        assertThat(InvalidUUIDException.class).isAssignableTo(DomainException.class);
        assertThat(InvalidStatusException.class).isAssignableTo(DomainException.class);
        assertThat(InvalidIntegerValue.class).isAssignableTo(DomainException.class);
        assertThat(InvalidAmountException.class).isAssignableTo(DomainException.class);
        assertThat(DomainException.class).isAssignableTo(RuntimeException.class);
    }

    @Test
    void preservanElMensaje() {
        assertThat(new InvalidStatusException("estado inválido").getMessage()).isEqualTo("estado inválido");
        assertThat(new InvalidUUIDException("uuid inválido").getMessage()).isEqualTo("uuid inválido");
        assertThat(new InvalidIntegerValue("entero inválido").getMessage()).isEqualTo("entero inválido");
        assertThat(new InvalidAmountException("monto inválido").getMessage()).isEqualTo("monto inválido");
    }
}