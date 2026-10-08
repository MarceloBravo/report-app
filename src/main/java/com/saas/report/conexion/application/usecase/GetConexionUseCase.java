package com.saas.report.conexion.application.usecase;

import com.saas.report.conexion.application.dto.ConexionDetailResponse;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Obtención de una única conexión a base de datos.
 *
 * <p>Además de buscarla por su identificador, valida que la conexión pertenezca
 * al tenant que la pide: si no existe o pertenece a otro tenant se lanza la
 * misma excepción, para no revelar conexiones ajenas.
 */
@Service
public class GetConexionUseCase {

    private final TenantDbConnectionRepository connectionRepository;

    public GetConexionUseCase(TenantDbConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    /**
     * Obtiene el detalle de una conexión del tenant.
     *
     * @param tenantId tenant autenticado, propietario de la conexión
     * @param dbConnectionId identificador de la conexión
     * @return el detalle de la conexión
     * @throws ResourceNotFoundException si la conexión no existe o no pertenece al tenant
     */
    @Transactional(readOnly = true)
    public ConexionDetailResponse obtener(TenantUUID tenantId, TenantDbConnectionUUID dbConnectionId) {
        TenantDbConnection connection = connectionRepository.findById(dbConnectionId)
                .filter(existing -> existing.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la conexión " + dbConnectionId.getValue()));

        return new ConexionDetailResponse(
                connection.getDbConnectionId().getValue(),
                connection.getHost(),
                connection.getDbName(),
                connection.getSchemaName());
    }
}
