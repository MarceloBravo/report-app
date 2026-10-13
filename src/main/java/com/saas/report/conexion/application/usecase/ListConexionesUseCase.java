package com.saas.report.conexion.application.usecase;

import com.saas.report.conexion.application.dto.ConexionListResponse;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.presentation.mapper.MotorBaseDatosPresenter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listado de las conexiones a bases de datos de un tenant.
 *
 * <p>El tenant siempre lo aporta el caller (resuelto del contexto de seguridad),
 * de modo que un tenant únicamente puede ver sus propias conexiones.
 */
@Service
public class ListConexionesUseCase {

    private final TenantDbConnectionRepository connectionRepository;

    public ListConexionesUseCase(TenantDbConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    /**
     * Devuelve el resumen de todas las conexiones del tenant.
     *
     * @param tenantId tenant cuyas conexiones se quieren listar
     * @return la lista de conexiones del tenant, vacía si no tiene ninguna
     */
    @Transactional(readOnly = true)
    public List<ConexionListResponse> listar(TenantUUID tenantId) {
        return connectionRepository.findAllByTenantId(tenantId).stream()
                .map(ListConexionesUseCase::toResponse)
                .toList();
    }

    private static ConexionListResponse toResponse(TenantDbConnection connection) {
        return new ConexionListResponse(
                connection.getDbConnectionId().getValue(),
                connection.getDbName(),
                connection.getSchemaName(),
                MotorBaseDatosPresenter.nombreAMostrar(connection.getMotorBd()));
    }
}
