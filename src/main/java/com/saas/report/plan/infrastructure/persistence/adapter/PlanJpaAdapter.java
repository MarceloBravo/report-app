package com.saas.report.plan.infrastructure.persistence.adapter;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.plan.infrastructure.persistence.PlanJpaRepository;
import com.saas.report.plan.infrastructure.persistence.mapper.PlanMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class PlanJpaAdapter implements PlanRepository {

    private final PlanJpaRepository repository;

    public PlanJpaAdapter(PlanJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> findById(PlanUUID planId) {
        return repository.findById(planId.getValue()).map(PlanMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> findAll() {
        return repository.findAllByOrderByNombrePlanAsc().stream()
                .map(PlanMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombrePlan(String nombrePlan) {
        return repository.existsByNombrePlan(nombrePlan);
    }

    @Override
    @Transactional
    public Plan save(Plan plan) {
        return PlanMapper.toDomain(repository.save(PlanMapper.toJpa(plan)));
    }
}