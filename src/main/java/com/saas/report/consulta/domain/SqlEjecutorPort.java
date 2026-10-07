package com.saas.report.consulta.domain;

import com.saas.report.conexion.domain.connection.TenantDbConnection;

import java.util.List;
import java.util.Map;

public interface SqlEjecutorPort {

    List<Map<String, Object>> ejecutar(TenantDbConnection conexion, String sql);
}
