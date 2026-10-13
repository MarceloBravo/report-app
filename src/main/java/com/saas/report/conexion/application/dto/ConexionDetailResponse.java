package com.saas.report.conexion.application.dto;

import java.util.UUID;

/**
 * Detalle de una conexión a base de datos.
 *
 * <p>Incluye el host para poder mostrar la conexión completa, pero nunca
 * expone el usuario ni la contraseña de la base.
 *
 * @param dbConnectionId identificador de la conexión
 * @param host host de la base de datos
 * @param dbName nombre de la base de datos
 * @param schemaName esquema por defecto de la conexión
 * @param motorBd motor de base de datos de la conexión
 */
public record ConexionDetailResponse(
        UUID dbConnectionId,
        String host,
        String dbName,
        String schemaName,
        String motorBd
) {
}
