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
            SELECT c.table_name,
                   c.column_name,
                   c.data_type,
                   c.is_nullable = 'YES' AS es_nullable,
                   EXISTS (
                       SELECT 1
                       FROM information_schema.table_constraints tc
                       JOIN information_schema.key_column_usage kcu
                         ON tc.constraint_name = kcu.constraint_name
                        AND tc.constraint_schema = kcu.constraint_schema
                        AND tc.table_schema = kcu.table_schema
                        AND tc.table_name = kcu.table_name
                       WHERE tc.constraint_type = 'PRIMARY KEY'
                         AND tc.table_schema = c.table_schema
                         AND tc.table_name = c.table_name
                         AND kcu.column_name = c.column_name
                   ) AS es_primary_key
            FROM information_schema.columns c
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
                            resultSet.getString("column_name"),
                            resultSet.getString("data_type"),
                            resultSet.getBoolean("es_nullable"),
                            resultSet.getBoolean("es_primary_key"));
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