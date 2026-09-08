package com.saas.report.plan.infrastructure.persistence.mapper;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.plan.infrastructure.persistence.PlanJpaEntity;
import com.saas.report.plan.infrastructure.persistence.PlanPrecioJpaEntity;

import java.util.ArrayList;
import java.util.List;

public final class PlanMapper {

    private PlanMapper() {
    }

    public static PlanJpaEntity toJpa(Plan plan) {
        PlanJpaEntity entity = new PlanJpaEntity();
        entity.setPlanId(plan.getPlanId().getValue());
        entity.setNombrePlan(plan.getNombrePlan());
        entity.setDescripcion(plan.getDescripcion());
        entity.setMaxUsuarios(plan.getMaxUsuarios().getValue());
        entity.setLimiteConsultasMensuales(plan.getLimiteConsultasMensuales().getValue());
        entity.setActivo(plan.isActivo());
        entity.setPrecios(new ArrayList<>(plan.getPrecios().stream().map(precio -> {
            PlanPrecioJpaEntity jpa = new PlanPrecioJpaEntity();
            jpa.setPlanPrecioId(precio.getPlanPrecioId().getValue());
            jpa.setPlan(entity);
            jpa.setMesDesde(precio.getMesDesde());
            jpa.setMesHasta(precio.getMesHasta());
            jpa.setPrecio(precio.getPrecio());
            return jpa;
        }).toList()));
        return entity;
    }

    public static Plan toDomain(PlanJpaEntity entity) {
        List<PlanPrecio> precios = entity.getPrecios().stream()
                .map(precio -> new PlanPrecio(
                        new PlanPrecioUUID(precio.getPlanPrecioId()),
                        new PlanUUID(entity.getPlanId()),
                        precio.getMesDesde(),
                        precio.getMesHasta(),
                        precio.getPrecio()))
                .toList();

        return new Plan(
                new PlanUUID(entity.getPlanId()),
                entity.getNombrePlan(),
                entity.getDescripcion(),
                new PlanMaxUsuarios(entity.getMaxUsuarios()),
                new PlanLimiteConsultasMensuales(entity.getLimiteConsultasMensuales()),
                entity.getActivo(),
                precios);
    }
}