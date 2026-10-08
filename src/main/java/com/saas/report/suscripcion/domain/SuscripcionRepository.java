package com.saas.report.suscripcion.domain;

import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository {

    Optional<Suscripcion> findById(SuscripcionUUID suscripcionId);

    Optional<Suscripcion> findByTenantIdAndEstado(TenantUUID tenantId, SuscripcionEstado estado);

    List<Suscripcion> findByTenantId(TenantUUID tenantId);

    Suscripcion save(Suscripcion suscripcion);
}