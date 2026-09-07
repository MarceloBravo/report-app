package com.saas.report.auth.domain.exception;

import com.saas.report.domain.exceptions.DomainException;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}