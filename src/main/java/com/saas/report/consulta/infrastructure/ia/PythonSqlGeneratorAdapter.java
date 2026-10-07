package com.saas.report.consulta.infrastructure.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.report.consulta.domain.GeneradorSqlPort;
import com.saas.report.consulta.domain.exception.ProveedorIaNoDisponibleException;
import com.saas.report.consulta.domain.exception.SqlInvalidoException;
import com.saas.report.conexion.domain.schema.ColumnaEsquema;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class PythonSqlGeneratorAdapter implements GeneradorSqlPort {

    private static final Logger log = LoggerFactory.getLogger(PythonSqlGeneratorAdapter.class);

    static final String ERROR_IA_NO_DEVOLVIO_SQL = "IA_NO_DEVOLVIO_SQL";

    private static final String MENSAJE_SERVICIO_NO_DISPONIBLE =
            "El servicio de IA no está disponible en este momento. Inténtelo más tarde.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PythonSqlGeneratorAdapter(@Value("${ai-service.base-url}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generarSql(EsquemaTenant esquema, String preguntaUsuario, UUID usuarioId) {
        SqlRequest sqlRequest = new SqlRequest(
                esquema.tenantId().getValue().toString(),
                usuarioId.toString(),
                preguntaUsuario,
                new EsquemaRequest(esquema.tablas().stream().map(this::toTabla).toList()));
        log.info("[PythonSqlGeneratorAdapter] Invocando microservicio IA POST baseUrl={} path=/sql contentType={} body={}",
                restClient, MediaType.APPLICATION_JSON, sqlRequest);
        SqlResponse response;
        try {
            response = restClient.post()
                    .uri("/sql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(sqlRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::manejarError)
                    .body(SqlResponse.class);
        } catch (RestClientException ex) {
            throw excepcionDeDominio(ex);
        }

        if (response == null || response.querySql() == null || response.querySql().isBlank()) {
            throw new SqlInvalidoException("El servicio de IA no devolvió un SQL válido.");
        }
        return response.querySql();
    }

    private void manejarError(HttpRequest request, ClientHttpResponse respuestaHttp) {
        SqlErrorResponse error = leerCuerpoError(respuestaHttp);
        if (error != null && ERROR_IA_NO_DEVOLVIO_SQL.equals(error.error())) {
            throw new SqlInvalidoException(
                    "El servicio de IA no devolvió un SQL válido (" + error.error() + "): " + detalle(error));
        }
        throw new ProveedorIaNoDisponibleException(MENSAJE_SERVICIO_NO_DISPONIBLE);
    }

    private SqlErrorResponse leerCuerpoError(ClientHttpResponse respuestaHttp) {
        try {
            String cuerpo = new String(respuestaHttp.getBody().readAllBytes(), StandardCharsets.UTF_8);
            if (cuerpo.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cuerpo, SqlErrorResponse.class);
        } catch (IOException ex) {
            log.warn("No se pudo interpretar el cuerpo de error del microservicio de IA", ex);
            return null;
        }
    }

    private String detalle(SqlErrorResponse error) {
        return error.detalle() == null || error.detalle().isBlank()
                ? "respuesta vacía o sin SQL parseable"
                : error.detalle();
    }

    private RuntimeException excepcionDeDominio(RestClientException causa) {
        for (Throwable c = causa; c != null; c = c.getCause()) {
            if (c instanceof SqlInvalidoException || c instanceof ProveedorIaNoDisponibleException) {
                return (RuntimeException) c;
            }
        }
        return new ProveedorIaNoDisponibleException(MENSAJE_SERVICIO_NO_DISPONIBLE, causa);
    }

    private TablaRequest toTabla(TablaEsquema tabla) {
        return new TablaRequest(tabla.nombre(), tabla.columnas().stream().map(this::toColumna).toList());
    }

    private ColumnaRequest toColumna(ColumnaEsquema columna) {
        return new ColumnaRequest(
                columna.nombre(), columna.tipoDato(), columna.nullable(), columna.esPrimaryKey(),
                columna.esForeignKey(), columna.tablaReferenciada(), columna.columnaReferenciada());
    }

    private record SqlRequest(String tenantId, String usuarioId, String preguntaUsuario, EsquemaRequest esquema) {
    }

    private record EsquemaRequest(List<TablaRequest> tablas) {
    }

    private record TablaRequest(String nombre, List<ColumnaRequest> columnas) {
    }

    private record ColumnaRequest(String nombre, String tipoDato, boolean nullable, boolean esPrimaryKey,
                                  boolean esForeignKey, String tablaReferenciada, String columnaReferenciada) {
    }

    private record SqlResponse(String tenantId, String querySql, Boolean exitosa, Integer tiempoMs) {
    }

    private record SqlErrorResponse(String error, String detalle, Integer tiempoMs) {
    }
}
