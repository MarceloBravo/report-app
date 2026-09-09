package com.saas.report.usuarios.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}