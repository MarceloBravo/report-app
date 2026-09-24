package com.saas.report.consulta.infrastructure.ia;

import com.saas.report.consulta.domain.exception.ProveedorIaNoDisponibleException;
import com.saas.report.consulta.domain.exception.SqlInvalidoException;
import com.saas.report.tenant.domain.schema.ColumnaEsquema;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PythonSqlGeneratorAdapterTest {

    private HttpServer servidor;
    private PythonSqlGeneratorAdapter adapter;

    private String ultimoCuerpoRecibido;

    @BeforeEach
    void setUp() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(0), 0);
        servidor.start();
        int puerto = servidor.getAddress().getPort();
        adapter = new PythonSqlGeneratorAdapter("http://localhost:" + puerto);
    }

    @AfterEach
    void tearDown() {
        servidor.stop(0);
    }

    @Test
    void devuelveElSqlCuandoElMicroservicioRespondeOk() {
        responder(HttpStatusCode.OK_200, ""
                .concat("{\"tenantId\":\"")
                .concat(UUID.randomUUID().toString())
                .concat("\",\"querySql\":\"SELECT * FROM ventas\",\"exitosa\":true,\"tiempoMs\":120}"),
                "application/json");

        String sql = adapter.generarSql(esquema(), "¿Cuántas ventas hubo?", UUID.randomUUID());

        assertThat(sql).isEqualTo("SELECT * FROM ventas");
    }

    @Test
    void lanzaSqlInvalidoCuandoIaNoDevuelveUnSqlValido() {
        responder(HttpStatusCode.SERVICE_UNAVAILABLE_503, ""
                .concat("{\"error\":\"IA_NO_DEVOLVIO_SQL\",")
                .concat("\"detalle\":\"la respuesta del proveedor de IA hace referencia a la tabla 'notebooks', que no existe en el esquema proporcionado\",")
                .concat("\"tiempoMs\":21500}"),
                "application/json");

        assertThatThrownBy(() -> adapter.generarSql(esquema(), "¿Cuántas notebooks hay?", UUID.randomUUID()))
                .isInstanceOf(SqlInvalidoException.class)
                .hasMessageContaining("IA_NO_DEVOLVIO_SQL")
                .hasMessageContaining("notebooks");
    }

    @Test
    void lanzaNoDisponibleCuandoElProveedorIaEstaIndisponible() {
        responder(HttpStatusCode.BAD_GATEWAY_502, ""
                .concat("{\"error\":\"PROVEEDOR_IA_INDISPONIBLE\",")
                .concat("\"detalle\":\"timeout del proveedor LLM\",")
                .concat("\"tiempoMs\":30000}"),
                "application/json");

        assertThatThrownBy(() -> adapter.generarSql(esquema(), "¿Cuántas ventas hubo?", UUID.randomUUID()))
                .isInstanceOf(ProveedorIaNoDisponibleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void lanzaNoDisponibleCuandoElCuerpoDeErrorNoEsJson() {
        responder(HttpStatusCode.BAD_GATEWAY_502, "502 Bad Gateway del proxy", "text/plain");

        assertThatThrownBy(() -> adapter.generarSql(esquema(), "¿Cuántas ventas hubo?", UUID.randomUUID()))
                .isInstanceOf(ProveedorIaNoDisponibleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void lanzaSqlInvalidoCuandoElMicroservicioDevuelveRespuestaVacia() {
        responder(HttpStatusCode.OK_200, "{\"tenantId\":\"x\",\"querySql\":\"\",\"exitosa\":true,\"tiempoMs\":5}",
                "application/json");

        assertThatThrownBy(() -> adapter.generarSql(esquema(), "¿Cuántas ventas hubo?", UUID.randomUUID()))
                .isInstanceOf(SqlInvalidoException.class)
                .hasMessageContaining("no devolvió un SQL");
    }

    @Test
    void enviaLasRelacionesDeClavesForaneasEnElEsquema() {
        responder(HttpStatusCode.OK_200, ""
                .concat("{\"tenantId\":\"")
                .concat(UUID.randomUUID().toString())
                .concat("\",\"querySql\":\"SELECT * FROM ventas\",\"exitosa\":true,\"tiempoMs\":120}"),
                "application/json");

        adapter.generarSql(esquema(), "¿Cuántas ventas hubo?", UUID.randomUUID());

        assertThat(ultimoCuerpoRecibido)
                .contains("\"esForeignKey\":true")
                .contains("\"tablaReferenciada\":\"clientes\"")
                .contains("\"columnaReferenciada\":\"id\"");
    }

    private void responder(int status, String cuerpo, String contentType) {
        servidor.createContext("/sql", exchange -> {
            ultimoCuerpoRecibido = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
            exchange.close();
        });
    }

    private EsquemaTenant esquema() {
        return new EsquemaTenant(TenantUUID.generate(),
                List.of(new TablaEsquema("ventas",
                        List.of(new ColumnaEsquema("ventas", "id", "uuid", false, true, false, null, null),
                                new ColumnaEsquema("ventas", "cliente_id", "uuid", false, false, true,
                                        "clientes", "id")))),
                Instant.now());
    }

    private final class HttpStatusCode {
        private static final int OK_200 = 200;
        private static final int BAD_GATEWAY_502 = 502;
        private static final int SERVICE_UNAVAILABLE_503 = 503;
    }
}