package com.saas.report.tenant.infrastructure.schema.factory;

import com.saas.report.tenant.infrastructure.schema.strategy.MySqlSchemaQueryProviderStrategy;
import com.saas.report.tenant.infrastructure.schema.strategy.PostgresqlSchemaQueryProviderStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaQueryFactoryTest {

    @Test
    void devuelveLaQueryDeMysqlCuandoElProductoEsMysql() {
        String query = SchemaQueryFactory.getSchemaQuery("MySQL");

        assertThat(query).isEqualTo(new MySqlSchemaQueryProviderStrategy().getSchemaQuery());
    }

    @Test
    void devuelveLaQueryDePostgresqlCuandoElProductoEsPostgresql() {
        String query = SchemaQueryFactory.getSchemaQuery("PostgreSQL");

        assertThat(query).isEqualTo(new PostgresqlSchemaQueryProviderStrategy().getSchemaQuery());
    }

    @ParameterizedTest
    @ValueSource(strings = { "mysql", "MySQL", "MYSQL", "MySql" })
    void resuelveMysqlSinImportarLaCapitalizacion(String dbType) {
        assertThat(SchemaQueryFactory.getSchemaQuery(dbType))
                .isEqualTo(SchemaQueryFactory.getSchemaQuery("mysql"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "postgresql", "PostgreSQL", "POSTGRESQL", "PostgreSql" })
    void resuelvePostgresqlSinImportarLaCapitalizacion(String dbType) {
        assertThat(SchemaQueryFactory.getSchemaQuery(dbType))
                .isEqualTo(SchemaQueryFactory.getSchemaQuery("postgresql"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "oracle", "MariaDB", "H2", "" })
    void lanzaExcepcionParaTiposDeBaseDeDatosNoSoportados(String dbType) {
        assertThatThrownBy(() -> SchemaQueryFactory.getSchemaQuery(dbType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported database type: " + dbType);
    }

    @Test
    void devuelveQueriesDistintasPorTipoDeBaseDeDatos() {
        assertThat(SchemaQueryFactory.getSchemaQuery("mysql"))
                .isNotEqualTo(SchemaQueryFactory.getSchemaQuery("postgresql"));
    }
}
