package com.saas.report.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanJpaRepository extends JpaRepository<PlanJpaEntity, UUID> {

    boolean existsByNombrePlan(String nombrePlan);

    List<PlanJpaEntity> findAllByOrderByNombrePlanAsc();
}