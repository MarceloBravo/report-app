package com.saas.report.conexion.infrastructure.schema;

import com.saas.report.conexion.domain.connection.CredentialCipher;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.conexion.infrastructure.schema.strategy.MySqlSchemaQueryProviderStrategy;
import com.saas.report.conexion.infrastructure.schema.strategy.PostgresqlSchemaQueryProviderStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcSchemaExtractorTest {

    @Mock
    private CredentialCipher credentialCipher;

    @Mock
    private DataSource appDataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    @Test
    void extraeTablasColumnasYClavesPrimarias() throws Exception {
        TenantUUID tenantId = TenantUUID.generate();
        TenantDbConnection conexion = new TenantDbConnection(
                TenantDbConnectionUUID.generate(), tenantId,
                "localhost", 5432, "report_db", "report_ro", "CIFRADO:secreto",
                TipoConexion.INTERNAL_SCHEMA, "public");

        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("table_name")).thenReturn("clientes", "clientes", "pedidos");
        when(resultSet.getString("column_name")).thenReturn("id", "nombre", "total");
        when(resultSet.getString("data_type")).thenReturn("integer", "character varying", "numeric");
        when(resultSet.getBoolean("es_nullable")).thenReturn(false, false, true);
        when(resultSet.getBoolean("es_primary_key")).thenReturn(true, false, false);
        when(resultSet.getBoolean("es_foreign_key")).thenReturn(false, false, true);
        when(resultSet.getString("tabla_referenciada")).thenReturn(null, null, "clientes");
        when(resultSet.getString("columna_referenciada")).thenReturn(null, null, "id");

        JdbcSchemaExtractor extractor = new JdbcSchemaExtractor(credentialCipher, appDataSource);

        EsquemaTenant esquema = extractor.extraer(conexion);

        assertThat(esquema.tenantId()).isEqualTo(tenantId);
        assertThat(esquema.tablas()).hasSize(2);
        TablaEsquema clientes = esquema.tablas().get(0);
        assertThat(clientes.nombre()).isEqualTo("clientes");
        assertThat(clientes.columnas()).hasSize(2);
        assertThat(clientes.columnas().get(0))
                .satisfies(columna -> {
                    assertThat(columna.nombre()).isEqualTo("id");
                    assertThat(columna.esPrimaryKey()).isTrue();
                    assertThat(columna.nullable()).isFalse();
                });
        assertThat(esquema.tablas().get(1).nombre()).isEqualTo("pedidos");
        assertThat(esquema.tablas().get(1).columnas())
                .singleElement()
                .satisfies(columna -> {
                    assertThat(columna.nombre()).isEqualTo("total");
                    assertThat(columna.esForeignKey()).isTrue();
                    assertThat(columna.tablaReferenciada()).isEqualTo("clientes");
                    assertThat(columna.columnaReferenciada()).isEqualTo("id");
                });
    }

    @Test
    void usaLaQueryDeLaEstrategiaDePostgresql() throws Exception {
        TenantDbConnection conexion = conexionCon(TipoConexion.INTERNAL_SCHEMA, "public");

        prepararConexion("PostgreSQL");
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        JdbcSchemaExtractor extractor = new JdbcSchemaExtractor(credentialCipher, appDataSource);
        extractor.extraer(conexion);

        verify(connection).prepareStatement(
                new PostgresqlSchemaQueryProviderStrategy().getSchemaQuery());
        verify(statement).setString(1, "public");
    }

    @Test
    void usaLaQueryDeLaEstrategiaDeMysql() throws Exception {
        TenantDbConnection conexion = conexionCon(TipoConexion.INTERNAL_SCHEMA, "reporte");

        prepararConexion("MySQL");
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        JdbcSchemaExtractor extractor = new JdbcSchemaExtractor(credentialCipher, appDataSource);
        extractor.extraer(conexion);

        verify(connection).prepareStatement(new MySqlSchemaQueryProviderStrategy().getSchemaQuery());
        verify(statement).setString(1, "reporte");
    }

    @Test
    void fallaConIllegalStateExceptionCuandoElMotorNoEsSoportado() throws Exception {
        TenantDbConnection conexion = conexionCon(TipoConexion.INTERNAL_SCHEMA, "public");

        prepararConexion("Oracle");

        JdbcSchemaExtractor extractor = new JdbcSchemaExtractor(credentialCipher, appDataSource);

        assertThatThrownBy(() -> extractor.extraer(conexion))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No se pudo extraer el esquema de la BD del tenant 'report_db'")
                .hasRootCauseMessage("Unsupported database type: Oracle");
    }

    private TenantDbConnection conexionCon(TipoConexion tipo, String schema) {
        return new TenantDbConnection(
                TenantDbConnectionUUID.generate(), TenantUUID.generate(),
                "localhost", 3306, "report_db", "report_ro", "CIFRADO:secreto",
                tipo, schema);
    }

    private void prepararConexion(String dbProduct) throws SQLException {
        when(appDataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(dbProduct);
    }
}