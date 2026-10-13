package com.saas.report.conexion.infrastructure.schema.factory;

import com.saas.report.conexion.infrastructure.schema.strategy.MySqlSchemaQueryProviderStrategy;
import com.saas.report.conexion.infrastructure.schema.strategy.PostgresqlSchemaQueryProviderStrategy;
import com.saas.report.shared.domain.valueobject.MotorBaseDatos;

/**
 * Selecciona la consulta de metadatos según el motor de base de datos.
 *
 * <p>Recibe el {@link MotorBaseDatos} ya resuelto (por ejemplo a partir de
 * {@code DatabaseMetaData.getDatabaseProductName()}) en lugar del nombre crudo
 * del producto, de modo que el reconocimiento del motor vive en un único sitio.
 * El {@code switch} exhaustivo obliga a contemplar la estrategia de cada motor
 * nuevo en tiempo de compilación.
 */
public class SchemaQueryFactory {

    public static String getSchemaQuery(MotorBaseDatos motor) {
        return switch (motor) {
            case MYSQL -> new MySqlSchemaQueryProviderStrategy().getSchemaQuery();
            case POSTGRESQL -> new PostgresqlSchemaQueryProviderStrategy().getSchemaQuery();
        };
    }
}
