package com.saas.report.consulta.domain;

import com.saas.report.tenant.domain.schema.EsquemaTenant;
import java.util.UUID;
public interface GeneradorSqlPort {

    String generarSql(EsquemaTenant esquema, String preguntaUsuario, UUID usuarioId);
}