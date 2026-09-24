package com.saas.report.tenant.infrastructure.schema;

import com.saas.report.tenant.domain.connection.CredentialCipher;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.schema.ColumnaEsquema;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.SchemaExtractor;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdbcSchemaExtractor implements SchemaExtractor {

    private static final String QUERY = """
            SELECT 
                c.table_name,
                c.column_name,
                c.data_type,
                c.is_identity,
                c.is_nullable = 'YES' AS es_nullable,
                
                -- Evaluación de PK
                EXISTS (
                    SELECT 1 
                    FROM information_schema.table_constraints tc_pk
                    JOIN information_schema.key_column_usage kcu_pk
                    ON tc_pk.constraint_name = kcu_pk.constraint_name
                    AND tc_pk.constraint_schema = kcu_pk.constraint_schema
                    WHERE tc_pk.constraint_type = 'PRIMARY KEY'
                    AND kcu_pk.table_schema = c.table_schema
                    AND kcu_pk.table_name = c.table_name
                    AND kcu_pk.column_name = c.column_name
                ) AS es_primary_key,

                -- Evaluación y datos de FK
                (fk.constraint_name IS NOT NULL) AS es_foreign_key,
                fk.tabla_referenciada,
                fk.columna_referenciada

            FROM information_schema.columns c

            -- Unimos únicamente las restricciones de tipo FOREIGN KEY para evitar duplicados por otros constraints
            LEFT JOIN (
                SELECT 
                    kcu.table_schema,
                    kcu.table_name,
                    kcu.column_name,
                    tc.constraint_name,
                    kcu_ref.table_name AS tabla_referenciada,
                    kcu_ref.column_name AS columna_referenciada
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name
                AND tc.constraint_schema = kcu.constraint_schema
                JOIN information_schema.referential_constraints rc
                ON tc.constraint_name = rc.constraint_name
                AND tc.constraint_schema = rc.constraint_schema
                JOIN information_schema.key_column_usage kcu_ref
                ON rc.unique_constraint_name = kcu_ref.constraint_name
                AND rc.unique_constraint_schema = kcu_ref.constraint_schema
                AND kcu.ordinal_position = kcu_ref.ordinal_position
                WHERE tc.constraint_type = 'FOREIGN KEY'
            ) fk 
            ON c.table_schema = fk.table_schema
            AND c.table_name = fk.table_name
            AND c.column_name = fk.column_name

            WHERE c.table_schema = ?
            AND c.table_name NOT LIKE 'pg_%'
            AND c.table_name NOT IN ('schema_migrations', 'flyway_schema_history')
            ORDER BY c.table_name, c.ordinal_position
            """;

    private final CredentialCipher credentialCipher;
    private final DataSource appDataSource;

    public JdbcSchemaExtractor(CredentialCipher credentialCipher, DataSource appDataSource) {
        this.credentialCipher = credentialCipher;
        this.appDataSource = appDataSource;
    }

    @Override
    public EsquemaTenant extraer(TenantDbConnection conexion) {
        try (Connection connection = abrirConexion(conexion)) {
            return consultar(connection, conexion);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo extraer el esquema de la BD del tenant '" + conexion.getDbName() + "'", e);
        }
    }

    private Connection abrirConexion(TenantDbConnection conexion) throws SQLException {
        if (conexion.esConexionExterna()) {
            String password = credentialCipher.descifrar(conexion.getDbPasswordEncrypted());
            return DriverManager.getConnection(
                    conexion.getConnectionString(), conexion.getDbUser(), password);
        }
        return appDataSource.getConnection();
    }

    private EsquemaTenant consultar(Connection connection, TenantDbConnection conexion) throws SQLException {
        Map<String, List<ColumnaEsquema>> tablasPorNombre = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setString(1, conexion.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String nombreTabla = resultSet.getString("table_name");
                    ColumnaEsquema columna = new ColumnaEsquema(
                            nombreTabla,
                            resultSet.getString("column_name"),
                            resultSet.getString("data_type"),
                            resultSet.getBoolean("es_nullable"),
                            resultSet.getBoolean("es_primary_key"),
                            resultSet.getBoolean("es_foreign_key"),
                            resultSet.getString("tabla_referenciada"),
                            resultSet.getString("columna_referenciada")
                        );
                    tablasPorNombre.computeIfAbsent(nombreTabla, k -> new ArrayList<>()).add(columna);
                }
            }
        }

        List<TablaEsquema> tablas = new ArrayList<>();
        tablasPorNombre.forEach((nombre, columnas) ->
                tablas.add(new TablaEsquema(nombre, List.copyOf(columnas))));

        return new EsquemaTenant(
                conexion.getTenantId(),
                List.copyOf(tablas),
                Instant.now());
    }
}