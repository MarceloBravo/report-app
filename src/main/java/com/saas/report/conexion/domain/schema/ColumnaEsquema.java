package com.saas.report.conexion.domain.schema;

public record ColumnaEsquema(
    String tabla, 
    String nombre, 
    String tipoDato, 
    boolean nullable, 
    boolean esPrimaryKey,
    boolean esForeignKey,
    String tablaReferenciada,
    String columnaReferenciada
) {
}