package com.saas.report.suscripcion.infrastructure.persistence.mapper;

import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionHistorialPrecio;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionHistorialPrecioUUID;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.suscripcion.infrastructure.persistence.SuscripcionHistorialPrecioJpaEntity;
import com.saas.report.suscripcion.infrastructure.persistence.SuscripcionJpaEntity;
import com.saas.report.shared.domain.valueobject.TenantUUID;

import java.util.ArrayList;

public final class SuscripcionMapper {

    private SuscripcionMapper() {
    }

    public static SuscripcionJpaEntity toJpa(Suscripcion suscripcion) {
        SuscripcionJpaEntity entity = new SuscripcionJpaEntity();
        entity.setSuscripcionId(suscripcion.getSuscripcionId().getValue());
        entity.setTenantId(suscripcion.getTenantId().getValue());
        entity.setPlanId(suscripcion.getPlanId().getValue());
        entity.setFechaInicio(suscripcion.getFechaInicio());
        entity.setEstado(suscripcion.getEstado());
        entity.setProximaFacturacion(suscripcion.getProximaFacturacion());

        entity.setHistorialPrecios(new ArrayList<>());
        suscripcion.getHistorialPrecios().forEach(historial -> {
            SuscripcionHistorialPrecioJpaEntity hijo = new SuscripcionHistorialPrecioJpaEntity();
            hijo.setHistorialId(historial.getHistorialId().getValue());
            hijo.setSuscripcion(entity);
            hijo.setPeriodoInicio(historial.getPeriodoInicio());
            hijo.setPeriodoFin(historial.getPeriodoFin());
            hijo.setPrecioAplicado(historial.getPrecioAplicado());
            hijo.setPlanPrecioId(historial.getPlanPrecioId().getValue());
            entity.getHistorialPrecios().add(hijo);
        });

        return entity;
    }

    public static Suscripcion toDomain(SuscripcionJpaEntity entity) {
        Suscripcion suscripcion = new Suscripcion(
                new SuscripcionUUID(entity.getSuscripcionId()),
                new TenantUUID(entity.getTenantId()),
                new PlanUUID(entity.getPlanId()),
                entity.getFechaInicio(),
                entity.getEstado(),
                entity.getProximaFacturacion());

        if (entity.getHistorialPrecios() != null) {
            entity.getHistorialPrecios().forEach(hijo -> suscripcion.addHistorialPrecio(
                    new SuscripcionHistorialPrecio(
                            new SuscripcionHistorialPrecioUUID(hijo.getHistorialId()),
                            suscripcion,
                            hijo.getPeriodoInicio(),
                            hijo.getPeriodoFin(),
                            hijo.getPrecioAplicado(),
                            new PlanPrecioUUID(hijo.getPlanPrecioId()))));
        }

        return suscripcion;
    }
}