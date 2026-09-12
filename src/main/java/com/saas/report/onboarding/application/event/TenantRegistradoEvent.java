package com.saas.report.onboarding.application.event;

import com.saas.report.tenant.domain.valueobject.TenantUUID;

public record TenantRegistradoEvent(TenantUUID tenantId) {
}