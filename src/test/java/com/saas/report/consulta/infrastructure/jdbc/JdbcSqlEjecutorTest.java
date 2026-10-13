package com.saas.report.consulta.infrastructure.jdbc;

import com.saas.report.consulta.domain.exception.ErrorEjecucionSqlException;
import com.saas.report.conexion.domain.connection.CredentialCipher;
import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcSqlEjecutorTest {

    @Mock
    private CredentialCipher credentialCipher;

    @Mock
    private DataSource appDataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    @Test
    void mapeaLasFilasUsandoLaEtiquetaDeCadaColumna() throws Exception {
        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT monto, ciudad FROM ventas")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnLabel(1)).thenReturn("monto");
        when(metaData.getColumnLabel(2)).thenReturn("ciudad");
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(150, 320);
        when(resultSet.getObject(2)).thenReturn("Bogotá", "Medellín");

        List<Map<String, Object>> filas = executor().ejecutar(conexion(TipoConexion.INTERNAL_SCHEMA), "SELECT monto, ciudad FROM ventas");

        assertThat(filas).containsExactly(
                Map.of("monto", 150, "ciudad", "Bogotá"),
                Map.of("monto", 320, "ciudad", "Medellín"));
    }

    @Test
    void desambiguaLasEtiquetasDeColumnaDuplicadas() throws Exception {
        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT 1 AS monto, 2 AS monto")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnLabel(1)).thenReturn("monto");
        when(metaData.getColumnLabel(2)).thenReturn("monto");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(150);
        when(resultSet.getObject(2)).thenReturn(320);

        List<Map<String, Object>> filas = executor().ejecutar(
                conexion(TipoConexion.INTERNAL_SCHEMA), "SELECT 1 AS monto, 2 AS monto");

        assertThat(filas).hasSize(1);
        assertThat(filas.get(0)).containsExactlyInAnyOrderEntriesOf(Map.of("monto", 150, "monto_2", 320));
    }

    @Test
    void fuerzaLaConexionASoloLecturaYAcotaFilasYTiempo() throws Exception {
        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT 1")).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.next()).thenReturn(false);

        executor(1000, 30).ejecutar(conexion(TipoConexion.INTERNAL_SCHEMA), "SELECT 1");

        verify(connection).setReadOnly(true);
        verify(statement).setMaxRows(1000);
        verify(statement).setQueryTimeout(30);
    }

    @Test
    void descifraLasCredencialesYNoUsaElDataSourceDeLaAppParaConexionesExternas() throws Exception {
        TenantDbConnection externa = new TenantDbConnection(
                TenantDbConnectionUUID.generate(), TenantUUID.generate(),
                "localhost", 1, "report_db", "report_ro", "CIFRADO:secreto",
                TipoConexion.EXTERNAL_DB, "public", MotorBaseDatos.POSTGRESQL);
        when(credentialCipher.descifrar("CIFRADO:secreto")).thenReturn("secreto");

        assertThatThrownBy(() -> executor().ejecutar(externa, "SELECT 1"))
                .isInstanceOf(ErrorEjecucionSqlException.class)
                .hasMessage("No se pudo ejecutar la consulta sobre la BD del tenant 'report_db'.");

        verify(credentialCipher).descifrar("CIFRADO:secreto");
        verify(appDataSource, never()).getConnection();
    }

    @Test
    void envuelveLosErroresDeJdbc() throws Exception {
        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new SQLException("sin permiso"));

        assertThatThrownBy(() -> executor().ejecutar(conexion(TipoConexion.INTERNAL_SCHEMA), "SELECT 1"))
                .isInstanceOf(ErrorEjecucionSqlException.class)
                .hasRootCauseMessage("sin permiso");
    }

    private JdbcSqlEjecutor executor() {
        return executor(1000, 30);
    }

    private JdbcSqlEjecutor executor(int maxRows, int timeout) {
        return new JdbcSqlEjecutor(credentialCipher, appDataSource, maxRows, timeout);
    }

    private TenantDbConnection conexion(TipoConexion tipo) {
        return new TenantDbConnection(
                TenantDbConnectionUUID.generate(), TenantUUID.generate(),
                "localhost", 5432, "report_db", "report_ro", "CIFRADO:secreto",
                tipo, "public", MotorBaseDatos.POSTGRESQL);
    }
}
