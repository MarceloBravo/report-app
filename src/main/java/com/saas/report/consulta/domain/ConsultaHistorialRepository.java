package com.saas.report.consulta.domain;

import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConsultaHistorialRepository {

    Optional<ConsultaHistorial> findById(ConsultaHistorialUUID consultaId);

    Page<ConsultaHistorial> findByTenantId(TenantUUID tenantId, Integer page, Integer size);

    long countByTenantIdAndFechaConsultaAfter(TenantUUID tenantId, LocalDateTime desde);

    ConsultaHistorial save(ConsultaHistorial consulta);
}