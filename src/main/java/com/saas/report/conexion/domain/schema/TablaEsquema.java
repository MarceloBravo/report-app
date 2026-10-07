package com.saas.report.conexion.domain.schema;

import java.util.List;

public record TablaEsquema(String nombre, List<ColumnaEsquema> columnas) {
}