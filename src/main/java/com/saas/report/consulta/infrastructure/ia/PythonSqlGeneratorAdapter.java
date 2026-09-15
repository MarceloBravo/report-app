package com.saas.report.consulta.infrastructure.ia;

import com.saas.report.consulta.domain.GeneradorSqlPort;
import com.saas.report.consulta.domain.exception.ProveedorIaNoDisponibleException;
import com.saas.report.consulta.domain.exception.SqlInvalidoException;
import com.saas.report.tenant.domain.schema.ColumnaEsquema;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class PythonSqlGeneratorAdapter implements GeneradorSqlPort {

    private final RestClient restClient;

    public PythonSqlGeneratorAdapter(@Value("${ai-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String generarSql(EsquemaTenant esquema, String preguntaUsuario) {
        SqlResponse response;
        try {
            response = restClient.post()
                    .uri("/sql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SqlRequest(
                            esquema.tenantId().getValue().toString(),
                            preguntaUsuario,
                            new EsquemaRequest(esquema.tablas().stream().map(this::toTabla).toList())))
                    .retrieve()
                    .body(SqlResponse.class);
        } catch (RestClientException ex) {
            throw new ProveedorIaNoDisponibleException(
                    "El servicio de IA no está disponible en este momento. Inténtelo más tarde.", ex);
        }

        if (response == null || response.querySql() == null || response.querySql().isBlank()) {
            throw new SqlInvalidoException("El servicio de IA no devolvió un SQL válido.");
        }
        return response.querySql();
    }

    private TablaRequest toTabla(TablaEsquema tabla) {
        return new TablaRequest(tabla.nombre(), tabla.columnas().stream().map(this::toColumna).toList());
    }

    private ColumnaRequest toColumna(ColumnaEsquema columna) {
        return new ColumnaRequest(columna.nombre(), columna.tipoDato(), columna.nullable(), columna.esPrimaryKey());
    }

    private record SqlRequest(String tenantId, String preguntaUsuario, EsquemaRequest esquema) {
    }

    private record EsquemaRequest(List<TablaRequest> tablas) {
    }

    private record TablaRequest(String nombre, List<ColumnaRequest> columnas) {
    }

    private record ColumnaRequest(String nombre, String tipoDato, boolean nullable, boolean esPrimaryKey) {
    }

    private record SqlResponse(String tenantId, String querySql, Boolean exitosa, Integer tiempoMs) {
    }
}