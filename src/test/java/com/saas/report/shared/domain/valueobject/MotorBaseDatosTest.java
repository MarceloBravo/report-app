package com.saas.report.shared.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MotorBaseDatosTest {

    @ParameterizedTest
    @ValueSource(strings = { "mysql", "MySQL", "MYSQL", "MySql", "MySQL 8.0" })
    void reconoceMysqlSinImportarLaCapitalizacionNiSufijos(String producto) {
        assertThat(MotorBaseDatos.desdeProducto(producto)).isEqualTo(MotorBaseDatos.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "postgresql", "PostgreSQL", "POSTGRESQL", "PostgreSql" })
    void reconocePostgresqlSinImportarLaCapitalizacion(String producto) {
        assertThat(MotorBaseDatos.desdeProducto(producto)).isEqualTo(MotorBaseDatos.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "oracle", "MariaDB", "H2", "SQL Server" })
    void lanzaExcepcionParaMotoresNoSoportados(String producto) {
        assertThatThrownBy(() -> MotorBaseDatos.desdeProducto(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motor de base de datos no soportado: " + producto);
    }

    @Test
    void lanzaExcepcionSiElNombreDelProductoEsNuloOVacio() {
        assertThatThrownBy(() -> MotorBaseDatos.desdeProducto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre del motor de base de datos no puede estar vacío");

        assertThatThrownBy(() -> MotorBaseDatos.desdeProducto("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre del motor de base de datos no puede estar vacío");
    }

    @Test
    void cadaMotorConoceSuSubprotocoloJdbc() {
        assertThat(MotorBaseDatos.POSTGRESQL.getSubprotocoloJdbc()).isEqualTo("postgresql");
        assertThat(MotorBaseDatos.MYSQL.getSubprotocoloJdbc()).isEqualTo("mysql");
    }
}
