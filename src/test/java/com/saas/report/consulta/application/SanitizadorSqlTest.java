package com.saas.report.consulta.application;

import com.saas.report.consulta.domain.exception.SqlInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SanitizadorSqlTest {

    @Test
    void aceptaUnSelectSimple() {
        String sql = SanitizadorSql.sanitizar("  SELECT * FROM ventas  ");
        assertThat(sql).isEqualTo("SELECT * FROM ventas");
    }

    @Test
    void rechazaUnSqlQueNoEmpiecePorSelect() {
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar("DELETE FROM ventas"));
    }

    @Test
    void rechazaUnSqlConPuntoYComa() {
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar("SELECT * FROM ventas;"));
    }

    @Test
    void rechazaUnSqlConComentarios() {
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar("SELECT * FROM ventas -- comentario"));
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar("SELECT * /* comentario */ FROM ventas"));
    }

    @Test
    void rechazaUnSqlNuloOVacio() {
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar(null));
        assertThrows(SqlInvalidoException.class, () -> SanitizadorSql.sanitizar(" "));
    }
}