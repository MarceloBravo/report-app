package com.saas.report.tenant.domain.schema;

import java.util.List;

public record TablaEsquema(String nombre, List<ColumnaEsquema> columnas) {
}