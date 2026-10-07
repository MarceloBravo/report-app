package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterConsultaUseCase {

    private final ConsultaHistorialRepository consultaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public RegisterConsultaUseCase(ConsultaHistorialRepository consultaRepository,
                                   SuscripcionRepository suscripcionRepository,
                                   PlanRepository planRepository) {
        this.consultaRepository = consultaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public ConsultaResponse registrar(ConsultaRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = ahora.toLocalDate().withDayOfMonth(1).atStartOfDay();
        TenantUUID tenantId = new TenantUUID(request.tenantId());

        long consultasPrevias = consultaRepository.countByTenantIdAndFechaConsultaAfter(tenantId, inicioPeriodo);
        Integer consultasEnPeriodo = Math.toIntExact(consultasPrevias + 1);

        validarLimiteMensual(tenantId, consultasEnPeriodo);

        ConsultaHistorial consulta = new ConsultaHistorial(
                ConsultaHistorialUUID.generate(),
                tenantId,
                new UsuarioUUID(request.usuarioId()),
                request.preguntaUsuario(),
                request.querySqlGenerada(),
                ahora,
                request.exitosa(),
                request.registrosCount(),
                request.dbConnectionId() != null ? new TenantDbConnectionUUID(request.dbConnectionId()) : null,
                consultasEnPeriodo);

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    private void validarLimiteMensual(TenantUUID tenantId, Integer consultasEnPeriodo) {
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