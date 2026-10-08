package com.saas.report.consulta.infrastructure.limite;

import com.saas.report.consulta.domain.ValidadorLimiteConsultasPort;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorLimiteConsultasAdapter implements ValidadorLimiteConsultasPort {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public ValidadorLimiteConsultasAdapter(SuscripcionRepository suscripcionRepository,
                                           PlanRepository planRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    @Override
    public void validarLimiteMensual(TenantUUID tenantId, Integer consultasEnPeriodo) {
        suscripcionRepository.findByTenantIdAndEstado(tenantId, SuscripcionEstado.ACTIVE)
                .flatMap(suscripcion -> planRepository.findById(suscripcion.getPlanId()))
                .ifPresent(plan -> {
                    if (!plan.permiteConsultas(consultasEnPeriodo)) {
                        throw new LimiteConsultasExcedidoException(
                                "El plan " + plan.getNombrePlan()
                                        + " ha superado su límite mensual de consultas ("
                                        + plan.getLimiteConsultasMensuales().getValue() + ").");
                    }
                });
    }
}