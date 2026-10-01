package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivatePlanUseCase {

    private final PlanRepository planRepository;
    private final String freePlanName;

    public DeactivatePlanUseCase(PlanRepository planRepository,
                                 @Value("${app.onboarding.free-plan-name}") String freePlanName) {
        this.planRepository = planRepository;
        this.freePlanName = freePlanName;
    }

    @Transactional
    public PlanResponse desactivar(PlanUUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan no encontrado con ID: " + planId.getValue()));

        if (plan.getNombrePlan().equalsIgnoreCase(freePlanName)) {
            throw new InvalidStatusException(
                    "El plan gratuito '" + freePlanName + "' no se puede desactivar porque el onboarding depende de él.");
        }

        plan.desactivar();

        return PlanResponse.from(planRepository.save(plan));
    }
}