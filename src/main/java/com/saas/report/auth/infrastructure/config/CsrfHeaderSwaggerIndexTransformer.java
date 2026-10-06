package com.saas.report.auth.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Inyecta en el {@code index.html} de Swagger UI un script que añade la
 * cabecera {@code X-Requested-With: XMLHttpRequest} a las requests que la UI
 * lanza desde el navegador, para que "Try it out" supere el
 * {@code CustomHeaderCsrfFilter} (que exige la cabecera cuando hay cookies de
 * sesión o en {@code /auth/login}).
 *
 * <p>Delega la transformación propia de springdoc en el transformer indicado y
 * solo interviene en el index de la UI (recurso {@code *.html} que contiene
 * {@code SwaggerUIBundle}); cualquier otro recurso estático pasa intacto.
 */
public class CsrfHeaderSwaggerIndexTransformer implements SwaggerIndexTransformer {

    /** Marca para no inyectar el script dos veces. */
    static final String MARCA = "__reportCabeceraCsrf";

    static final String SCRIPT = """
            <script>
            (function () {
                if (window.__reportCabeceraCsrf) { return; }
                window.__reportCabeceraCsrf = true;
                var NOMBRE = 'X-Requested-With';
                var VALOR = 'XMLHttpRequest';
                var fetchOriginal = window.fetch;
                if (fetchOriginal) {
                    window.fetch = function (input, init) {
                        init = init || {};
                        var encabezados = new Headers(input && input.headers ? input.headers : undefined);
                        new Headers(init.headers).forEach(function (valor, nombre) {
                            encabezados.set(nombre, valor);
                        });
                        if (!encabezados.has(NOMBRE)) { encabezados.set(NOMBRE, VALOR); }
                        init.headers = encabezados;
                        return fetchOriginal.call(window, input, init);
                    };
                }
                var xhr = window.XMLHttpRequest;
                if (xhr) {
                    var sendOriginal = xhr.prototype.send;
                    xhr.prototype.send = function () {
                        try {
                            if (!this.getRequestHeader(NOMBRE)) { this.setRequestHeader(NOMBRE, VALOR); }
                        } catch (error) { }
                        return sendOriginal.apply(this, arguments);
                    };
                }
            })();
            </script>
            """;

    private final SwaggerIndexTransformer delegado;

    public CsrfHeaderSwaggerIndexTransformer(SwaggerIndexTransformer delegado) {
        this.delegado = delegado;
    }

    @Override
    public Resource transform(HttpServletRequest request, Resource resource,
                              ResourceTransformerChain chain) throws IOException {
        Resource transformado = delegado.transform(request, resource, chain);
        if (transformado == null || !esPaginaHtml(transformado)) {
            return transformado;
        }
        String html = StreamUtils.copyToString(transformado.getInputStream(), StandardCharsets.UTF_8);
        if (!html.contains("SwaggerUIBundle") || html.contains(MARCA) || !html.contains("</body>")) {
            return transformado;
        }
        int cierreBody = html.lastIndexOf("</body>");
        String inyectado = html.substring(0, cierreBody) + SCRIPT + html.substring(cierreBody);
        return new TransformedResource(transformado, inyectado.getBytes(StandardCharsets.UTF_8));
    }

    private boolean esPaginaHtml(Resource resource) {
        String nombre = resource.getFilename();
        return nombre != null && nombre.toLowerCase(Locale.ROOT).endsWith(".html");
    }
}
