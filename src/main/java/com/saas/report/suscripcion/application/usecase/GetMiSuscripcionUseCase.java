package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMiSuscripcionUseCase {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public GetMiSuscripcionUseCase(SuscripcionRepository suscripcionRepository, PlanRepository planRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public MiSuscripcionResponse obtenerMiSuscripcion(TenantUUID tenantId) {
        Suscripcion suscripcion = suscripcionRepository.findByTenantId(tenantId).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El tenant no tiene una suscripción registrada"));

        Plan plan = planRepository.findById(suscripcion.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan no encontrado con ID: " + suscripcion.getPlanId().getValue()));

        return MiSuscripcionResponse.from(suscripcion, plan);
    }
}