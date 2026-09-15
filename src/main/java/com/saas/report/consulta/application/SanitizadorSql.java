package com.saas.report.consulta.application;

import com.saas.report.consulta.domain.exception.SqlInvalidoException;

public final class SanitizadorSql {

    private SanitizadorSql() {
    }

    public static String sanitizar(String sql) {
        String sqlLimpio = sql == null ? "" : sql.trim();
        if (!sqlLimpio.toUpperCase().startsWith("SELECT")) {
            throw new SqlInvalidoException("El SQL generado debe ser una consulta de solo lectura (SELECT).");
        }
        if (sqlLimpio.contains(";")) {
            throw new SqlInvalidoException("El SQL generado no puede contener el carácter ';'.");
        }
        if (sqlLimpio.contains("--") || sqlLimpio.contains("/*") || sqlLimpio.contains("*/")) {
            throw new SqlInvalidoException("El SQL generado no puede contener comentarios.");
        }
        return sqlLimpio;
    }
}