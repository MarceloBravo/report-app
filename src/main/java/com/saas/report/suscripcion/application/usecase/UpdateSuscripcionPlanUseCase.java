package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.ActualizarPlanRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSuscripcionPlanUseCase {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public UpdateSuscripcionPlanUseCase(SuscripcionRepository suscripcionRepository, PlanRepository planRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public SuscripcionResponse actualizarPlan(SuscripcionUUID suscripcionId, ActualizarPlanRequest request) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Suscripción no encontrada con ID: " + suscripcionId.getValue()));

        Plan plan = planRepository.findById(new PlanUUID(request.planId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan no encontrado con ID: " + request.planId()));

        suscripcion.actualizarPlan(plan);

        return SuscripcionResponse.from(suscripcionRepository.save(suscripcion));
    }
}