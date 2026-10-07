package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.SanitizadorSql;
import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.GeneradorSqlPort;
import com.saas.report.consulta.domain.SqlEjecutorPort;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.SchemaExtractor;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@Service
public class ConvertirConsultaUseCase {

    private final ConsultaHistorialRepository consultaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;
    private final TenantDbConnectionRepository dbConnectionRepository;
    private final SchemaExtractor schemaExtractor;
    private final GeneradorSqlPort generadorSqlPort;
    private final SqlEjecutorPort sqlEjecutorPort;

    public ConvertirConsultaUseCase(ConsultaHistorialRepository consultaRepository,
                                    SuscripcionRepository suscripcionRepository,
                                    PlanRepository planRepository,
                                    TenantDbConnectionRepository dbConnectionRepository,
                                    SchemaExtractor schemaExtractor,
                                    GeneradorSqlPort generadorSqlPort,
                                    SqlEjecutorPort sqlEjecutorPort) {
        this.consultaRepository = consultaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
        this.dbConnectionRepository = dbConnectionRepository;
        this.schemaExtractor = schemaExtractor;
        this.generadorSqlPort = generadorSqlPort;
        this.sqlEjecutorPort = sqlEjecutorPort;
    }

    @Transactional
    public ConsultaResponse convertir(UUID tenantId, UUID usuarioId, ConvertirConsultaRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = ahora.toLocalDate().withDayOfMonth(1).atStartOfDay();
        TenantUUID tenant = new TenantUUID(tenantId);

        long consultasPrevias = consultaRepository.countByTenantIdAndFechaConsultaAfter(tenant, inicioPeriodo);
        Integer consultasEnPeriodo = Math.toIntExact(consultasPrevias + 1);

        validarLimiteMensual(tenant, consultasEnPeriodo);

        TenantDbConnection conexion = resolverConexion(tenant, request.dbConnectionId());
        EsquemaTenant esquema = schemaExtractor.extraer(conexion);

        String sqlGenerado = generadorSqlPort.generarSql(esquema, request.preguntaUsuario(), usuarioId);
        String sqlSanitizado = SanitizadorSql.sanitizar(sqlGenerado);

        List<Map<String, Object>> resultado = sqlEjecutorPort.ejecutar(conexion, sqlSanitizado);

        ConsultaHistorial consulta = new ConsultaHistorial(
                ConsultaHistorialUUID.generate(),
                tenant,
                new UsuarioUUID(usuarioId),
                request.preguntaUsuario(),
                sqlSanitizado,
                ahora,
                true,
                resultado.size(),
                conexion.getDbConnectionId(),
                consultasEnPeriodo);

        return ConsultaResponse.from(consultaRepository.save(consulta), resultado);
    }

    private TenantDbConnection resolverConexion(TenantUUID tenantId, UUID dbConnectionId) {
        Optional<TenantDbConnection> conexion = dbConnectionId != null
                ? dbConnectionRepository.findById(new TenantDbConnectionUUID(dbConnectionId))
                        .filter(c -> tenantId.equals(c.getTenantId()))
                : dbConnectionRepository.findByTenantId(tenantId);

        return conexion.orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró una conexión de base de datos para el tenant " + tenantId.getValue() + "."));
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