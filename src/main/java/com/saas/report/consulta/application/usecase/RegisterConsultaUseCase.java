package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.ValidadorLimiteConsultasPort;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterConsultaUseCase {

    private final ConsultaHistorialRepository consultaRepository;
    private final ValidadorLimiteConsultasPort validadorLimiteConsultasPort;

    public RegisterConsultaUseCase(ConsultaHistorialRepository consultaRepository,
                                   ValidadorLimiteConsultasPort validadorLimiteConsultasPort) {
        this.consultaRepository = consultaRepository;
        this.validadorLimiteConsultasPort = validadorLimiteConsultasPort;
    }

    @Transactional
    public ConsultaResponse registrar(ConsultaRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = ahora.toLocalDate().withDayOfMonth(1).atStartOfDay();
        TenantUUID tenantId = new TenantUUID(request.tenantId());

        long consultasPrevias = consultaRepository.countByTenantIdAndFechaConsultaAfter(tenantId, inicioPeriodo);
        Integer consultasEnPeriodo = Math.toIntExact(consultasPrevias + 1);

        validadorLimiteConsultasPort.validarLimiteMensual(tenantId, consultasEnPeriodo);

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
                consultasEnPeriodo,
                null);

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }
}