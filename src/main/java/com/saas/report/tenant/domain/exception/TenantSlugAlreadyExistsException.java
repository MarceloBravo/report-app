package com.saas.report.tenant.domain.exception;

import com.saas.report.shared.exception.DomainException;

public class TenantSlugAlreadyExistsException extends DomainException {

    public TenantSlugAlreadyExistsException(String message) {
        super(message);
    }

    public TenantSlugAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}