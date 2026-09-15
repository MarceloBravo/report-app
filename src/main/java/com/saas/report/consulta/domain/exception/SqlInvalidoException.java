package com.saas.report.consulta.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class SqlInvalidoException extends DomainException {

    public SqlInvalidoException(String message) {
        super(message);
    }

}