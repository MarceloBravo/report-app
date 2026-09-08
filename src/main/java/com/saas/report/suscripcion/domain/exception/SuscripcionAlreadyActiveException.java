package com.saas.report.suscripcion.domain.exception;

public class SuscripcionAlreadyActiveException extends RuntimeException {

    public SuscripcionAlreadyActiveException(String message) {
        super(message);
    }
}