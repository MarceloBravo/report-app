package com.saas.report.suscripcion.domain;

import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.util.Optional;

public interface SuscripcionRepository {

    Optional<Suscripcion> findById(SuscripcionUUID suscripcionId);

    Optional<Suscripcion> findByTenantIdAndEstado(TenantUUID tenantId, SuscripcionEstado estado);

    Suscripcion save(Suscripcion suscripcion);
}