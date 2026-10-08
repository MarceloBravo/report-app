package com.saas.report.conexion.domain.schema;

import com.saas.report.shared.domain.valueobject.TenantUUID;

import java.time.Instant;
import java.util.List;

public record EsquemaTenant(TenantUUID tenantId, List<TablaEsquema> tablas, Instant extraidoEn) {
}