package com.saas.report.consulta.domain.exception;

public class LimiteConsultasExcedidoException extends RuntimeException {

    public LimiteConsultasExcedidoException(String message) {
        super(message);
    }
}