package com.saas.report.conexion.infrastructure.schema.factory;

import com.saas.report.conexion.infrastructure.schema.strategy.MySqlSchemaQueryProviderStrategy;
import com.saas.report.conexion.infrastructure.schema.strategy.PostgresqlSchemaQueryProviderStrategy;
import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaQueryFactoryTest {

    @Test
    void devuelveLaQueryDeMysqlParaElMotorMysql() {
        String query = SchemaQueryFactory.getSchemaQuery(MotorBaseDatos.MYSQL);

        assertThat(query).isEqualTo(new MySqlSchemaQueryProviderStrategy().getSchemaQuery());
    }

    @Test
    void devuelveLaQueryDePostgresqlParaElMotorPostgresql() {
        String query = SchemaQueryFactory.getSchemaQuery(MotorBaseDatos.POSTGRESQL);

        assertThat(query).isEqualTo(new PostgresqlSchemaQueryProviderStrategy().getSchemaQuery());
    }

    @Test
    void devuelveQueriesDistintasPorMotor() {
        assertThat(SchemaQueryFactory.getSchemaQuery(MotorBaseDatos.MYSQL))
                .isNotEqualTo(SchemaQueryFactory.getSchemaQuery(MotorBaseDatos.POSTGRESQL));
    }
}
