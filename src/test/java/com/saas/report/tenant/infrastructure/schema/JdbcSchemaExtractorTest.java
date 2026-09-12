package com.saas.report.tenant.infrastructure.schema;

import com.saas.report.tenant.domain.connection.CredentialCipher;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.connection.TipoConexion;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("table_name")).thenReturn("clientes", "clientes", "pedidos");
        when(resultSet.getString("column_name")).thenReturn("id", "nombre", "total");
        when(resultSet.getString("data_type")).thenReturn("integer", "character varying", "numeric");
        when(resultSet.getBoolean("es_nullable")).thenReturn(false, false, true);
        when(resultSet.getBoolean("es_primary_key")).thenReturn(true, false, false);

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
    }
}