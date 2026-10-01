package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivatePlanUseCase {

    private final PlanRepository planRepository;

    public ActivatePlanUseCase(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public PlanResponse activar(PlanUUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan no encontrado con ID: " + planId.getValue()));

        plan.activar();

        return PlanResponse.from(planRepository.save(plan));
    }
}