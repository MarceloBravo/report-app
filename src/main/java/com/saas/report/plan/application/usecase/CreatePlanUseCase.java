package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanRequest;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.exception.PlanNombreAlreadyExistsException;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreatePlanUseCase {

    private final PlanRepository planRepository;

    public CreatePlanUseCase(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public PlanResponse crear(PlanRequest request) {
        if (planRepository.existsByNombrePlan(request.nombrePlan())) {
            throw new PlanNombreAlreadyExistsException(
                    "Ya existe un plan con el nombre '" + request.nombrePlan() + "'");
        }

        PlanUUID planId = PlanUUID.generate();
        List<PlanPrecio> precios = request.precios().stream()
                .map(precio -> new PlanPrecio(
                        PlanPrecioUUID.generate(),
                        planId,
                        precio.mesDesde(),
                        precio.mesHasta(),
                        precio.precio()))
                .toList();

        Plan plan = new Plan(
                planId,
                request.nombrePlan(),
                request.descripcion(),
                new PlanMaxUsuarios(request.maxUsuarios()),
                new PlanLimiteConsultasMensuales(request.limiteConsultasMensuales()),
                request.activo(),
                precios);

        return PlanResponse.from(planRepository.save(plan));
    }
}