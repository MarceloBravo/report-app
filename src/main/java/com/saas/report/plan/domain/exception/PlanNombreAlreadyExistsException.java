package com.saas.report.plan.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class PlanNombreAlreadyExistsException extends DomainException {

    public PlanNombreAlreadyExistsException(String message) {
        super(message);
    }

    public PlanNombreAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}