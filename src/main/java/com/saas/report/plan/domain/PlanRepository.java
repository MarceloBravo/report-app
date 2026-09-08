package com.saas.report.plan.domain;

import com.saas.report.plan.domain.valueobject.PlanUUID;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {

    Optional<Plan> findById(PlanUUID planId);

    List<Plan> findAll();

    boolean existsByNombrePlan(String nombrePlan);

    Plan save(Plan plan);
}