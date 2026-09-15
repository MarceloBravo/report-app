package com.saas.report.consulta.domain.exception;

public class ProveedorIaNoDisponibleException extends RuntimeException {

    public ProveedorIaNoDisponibleException(String message) {
        super(message);
    }

    public ProveedorIaNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}