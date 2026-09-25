package com.saas.report.consulta.infrastructure.jdbc;

import com.saas.report.consulta.domain.SqlEjecutorPort;
import com.saas.report.consulta.domain.exception.ErrorEjecucionSqlException;
import com.saas.report.tenant.domain.connection.CredentialCipher;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdbcSqlEjecutor implements SqlEjecutorPort {

    private final CredentialCipher credentialCipher;
    private final DataSource appDataSource;
    private final int maxRows;
    private final int queryTimeoutSeconds;

    public JdbcSqlEjecutor(
            CredentialCipher credentialCipher,
            DataSource appDataSource,
            @Value("${app.consultas.max-rows:1000}") int maxRows,
            @Value("${app.consultas.query-timeout-seconds:30}") int queryTimeoutSeconds) {
        this.credentialCipher = credentialCipher;
        this.appDataSource = appDataSource;
        this.maxRows = maxRows;
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    @Override
    public List<Map<String, Object>> ejecutar(TenantDbConnection conexion, String sql) {
        try (Connection connection = abrirConexion(conexion)) {
            connection.setReadOnly(true);
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                statement.setQueryTimeout(queryTimeoutSeconds);
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    return mapearFilas(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new ErrorEjecucionSqlException(
                    "No se pudo ejecutar la consulta sobre la BD del tenant '" + conexion.getDbName() + "'.", e);
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

    private List<Map<String, Object>> mapearFilas(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int totalColumnas = metaData.getColumnCount();
        List<Map<String, Object>> filas = new ArrayList<>();

        while (resultSet.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= totalColumnas; i++) {
                fila.put(etiquetaUnica(metaData, i), resultSet.getObject(i));
            }
            filas.add(fila);
        }
        return List.copyOf(filas);
    }

    private String etiquetaUnica(ResultSetMetaData metaData, int indice) throws SQLException {
        String etiqueta = metaData.getColumnLabel(indice);
        if (etiqueta == null || etiqueta.isBlank()) {
            etiqueta = metaData.getColumnName(indice);
        }
        int repeticiones = 0;
        for (int i = 1; i < indice; i++) {
            if (coincide(metaData, i, etiqueta)) {
                repeticiones++;
            }
        }
        return repeticiones == 0 ? etiqueta : etiqueta + "_" + (repeticiones + 1);
    }

    private boolean coincide(ResultSetMetaData metaData, int indice, String etiqueta) throws SQLException {
        return etiqueta.equalsIgnoreCase(metaData.getColumnLabel(indice))
                || etiqueta.equalsIgnoreCase(metaData.getColumnName(indice));
    }
}
