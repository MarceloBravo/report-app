package com.saas.report.conexion.infrastructure.schema;

import com.saas.report.conexion.domain.connection.CredentialCipher;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.schema.ColumnaEsquema;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.SchemaExtractor;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import org.springframework.stereotype.Component;

import com.saas.report.conexion.infrastructure.schema.factory.SchemaQueryFactory;

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
    /*
    private static final String QUERY = """
            
            """;
    */
    private final CredentialCipher credentialCipher;
    private final DataSource appDataSource;

    public JdbcSchemaExtractor(CredentialCipher credentialCipher, DataSource appDataSource) {
        this.credentialCipher = credentialCipher;
        this.appDataSource = appDataSource;
    }

    @Override
    public EsquemaTenant extraer(TenantDbConnection conexion) {
        try (Connection connection = abrirConexion(conexion)) {
            String dbType = connection.getMetaData().getDatabaseProductName();
            String schemaQuery = SchemaQueryFactory.getSchemaQuery(dbType);
            return consultar(connection, conexion, schemaQuery);
        } catch (SQLException | IllegalArgumentException e) {
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

    private EsquemaTenant consultar(
            Connection connection,
            TenantDbConnection conexion,
            String schemaQuery) throws SQLException {
        Map<String, List<ColumnaEsquema>> tablasPorNombre = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(schemaQuery)) {
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