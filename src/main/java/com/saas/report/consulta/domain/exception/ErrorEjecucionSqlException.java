package com.saas.report.consulta.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class ErrorEjecucionSqlException extends DomainException {

    public ErrorEjecucionSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
