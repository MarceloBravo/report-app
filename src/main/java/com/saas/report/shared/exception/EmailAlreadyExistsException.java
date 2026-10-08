package com.saas.report.shared.exception;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}