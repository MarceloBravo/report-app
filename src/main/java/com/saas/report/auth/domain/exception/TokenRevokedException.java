package com.saas.report.auth.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class TokenRevokedException extends DomainException {

    public TokenRevokedException(String message) {
        super(message);
    }

    public TokenRevokedException(String message, Throwable cause) {
        super(message, cause);
    }
}