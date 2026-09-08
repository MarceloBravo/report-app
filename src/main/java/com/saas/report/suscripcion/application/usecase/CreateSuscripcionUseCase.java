package com.saas.report.suscripcion.application.usecase;

import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.SuscripcionRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionHistorialPrecio;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.exception.SuscripcionAlreadyActiveException;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionHistorialPrecioUUID;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CreateSuscripcionUseCase {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public CreateSuscripcionUseCase(SuscripcionRepository suscripcionRepository, PlanRepository planRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public SuscripcionResponse crear(SuscripcionRequest request) {
        TenantUUID tenantId = new TenantUUID(request.tenantId());
        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : LocalDate.now();

        if (suscripcionRepository.findByTenantIdAndEstado(tenantId, SuscripcionEstado.ACTIVE).isPresent()
                || suscripcionRepository.findByTenantIdAndEstado(tenantId, SuscripcionEstado.SUSPENDED).isPresent()) {
            throw new SuscripcionAlreadyActiveException(
                    "El tenant ya tiene una suscripción activa o suspendida.");
        }

        Plan plan = planRepository.findById(new PlanUUID(request.planId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan no encontrado con ID: " + request.planId()));
        if (!plan.isActivo()) {
            throw new InvalidStatusException("El plan de destino no está activo.");
        }

        Suscripcion suscripcion = new Suscripcion(
                SuscripcionUUID.generate(),
                tenantId,
                plan.getPlanId(),
                fechaInicio,
                SuscripcionEstado.ACTIVE,
                fechaInicio.plusMonths(1));

        PlanPrecio tramoInicial = plan.getPrecioParaMes(1);
        suscripcion.addHistorialPrecio(new SuscripcionHistorialPrecio(
                SuscripcionHistorialPrecioUUID.generate(),
                suscripcion,
                fechaInicio,
                null,
                tramoInicial.getPrecio(),
                tramoInicial.getPlanPrecioId()));

        return SuscripcionResponse.from(suscripcionRepository.save(suscripcion));
    }
}