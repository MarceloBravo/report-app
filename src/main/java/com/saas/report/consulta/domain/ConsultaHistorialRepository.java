package com.saas.report.consulta.domain;

import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaHistorialRepository {

    Optional<ConsultaHistorial> findById(ConsultaHistorialUUID consultaId);

    List<ConsultaHistorial> findByTenantId(TenantUUID tenantId);

    long countByTenantIdAndFechaConsultaAfter(TenantUUID tenantId, LocalDateTime desde);

    ConsultaHistorial save(ConsultaHistorial consulta);
}