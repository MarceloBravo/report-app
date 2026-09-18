package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.SanitizadorSql;
import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.GeneradorSqlPort;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.connection.TenantDbConnectionRepository;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.SchemaExtractor;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConvertirConsultaUseCase {

    private final ConsultaHistorialRepository consultaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;
    private final TenantDbConnectionRepository dbConnectionRepository;
    private final SchemaExtractor schemaExtractor;
    private final GeneradorSqlPort generadorSqlPort;

    public ConvertirConsultaUseCase(ConsultaHistorialRepository consultaRepository,
                                    SuscripcionRepository suscripcionRepository,
                                    PlanRepository planRepository,
                                    TenantDbConnectionRepository dbConnectionRepository,
                                    SchemaExtractor schemaExtractor,
                                    GeneradorSqlPort generadorSqlPort) {
        this.consultaRepository = consultaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
        this.dbConnectionRepository = dbConnectionRepository;
        this.schemaExtractor = schemaExtractor;
        this.generadorSqlPort = generadorSqlPort;
    }

    @Transactional
    public ConsultaResponse convertir(ConvertirConsultaRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = ahora.toLocalDate().withDayOfMonth(1).atStartOfDay();
        TenantUUID tenantId = new TenantUUID(request.tenantId());

        long consultasPrevias = consultaRepository.countByTenantIdAndFechaConsultaAfter(tenantId, inicioPeriodo);
        Integer consultasEnPeriodo = Math.toIntExact(consultasPrevias + 1);

        validarLimiteMensual(tenantId, consultasEnPeriodo);

        EsquemaTenant esquema = extraerEsquema(tenantId, request.dbConnectionId());

        String sqlGenerado = generadorSqlPort.generarSql(esquema, request.preguntaUsuario(), request.usuarioId());
        String sqlSanitizado = SanitizadorSql.sanitizar(sqlGenerado);

        ConsultaHistorial consulta = new ConsultaHistorial(
                ConsultaHistorialUUID.generate(),
                tenantId,
                new UsuarioUUID(request.usuarioId()),
                request.preguntaUsuario(),
                sqlSanitizado,
                ahora,
                null,
                null,
                request.dbConnectionId() != null ? new TenantDbConnectionUUID(request.dbConnectionId()) : null,
                consultasEnPeriodo);

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    private EsquemaTenant extraerEsquema(TenantUUID tenantId, UUID dbConnectionId) {
        Optional<TenantDbConnection> conexion = dbConnectionId != null
                ? dbConnectionRepository.findById(new TenantDbConnectionUUID(dbConnectionId))
                : dbConnectionRepository.findByTenantId(tenantId);

        TenantDbConnection encontrada = conexion.orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró una conexión de base de datos para el tenant " + tenantId.getValue() + "."));

        return schemaExtractor.extraer(encontrada);
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