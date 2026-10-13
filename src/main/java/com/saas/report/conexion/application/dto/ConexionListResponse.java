package com.saas.report.conexion.application.dto;

import java.util.UUID;

/**
 * Resumen de una conexión a base de datos para el listado.
 *
 * <p>Solo expone los datos necesarios para identificar la conexión en una
 * tabla: nunca la contraseña ni el usuario de la base.
 *
 * @param dbConnectionId identificador de la conexión
 * @param dbName nombre de la base de datos
 * @param schemaName esquema por defecto de la conexión
 * @param motorBd motor de base de datos de la conexión
 */
public record ConexionListResponse(
        UUID dbConnectionId,
        String dbName,
        String schemaName,
        String motorBd
) {
}
