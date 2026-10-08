package com.saas.report.consulta.domain;

import com.saas.report.shared.domain.valueobject.TenantUUID;

public interface ValidadorLimiteConsultasPort {
    void validarLimiteMensual(TenantUUID tenantId, Integer consultasEnPeriodo);
}