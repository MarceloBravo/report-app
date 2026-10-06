package com.saas.report.auth.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifica que el transformer inyecte el script de la cabecera CSRF solo en el
 * index de Swagger UI y que no duplique ni toque otros recursos.
 */
class CsrfHeaderSwaggerIndexTransformerTest {

    private static final String INDEX = """
            <html><head><script src="swagger-ui-bundle.js"></script></head>
            <body><script>window.onload = SwaggerUIBundle({});</script></body></html>
            """;

    private final SwaggerIndexTransformer delegado = mock(SwaggerIndexTransformer.class);

    @Test
    @DisplayName("Inyecta el script antes de </body> en el index de Swagger UI")
    void inyectaElScriptEnElIndex() throws Exception {
        Resource original = html("index.html", INDEX);
        when(delegado.transform(any(), any(), any())).thenReturn(original);

        Resource resultado = new CsrfHeaderSwaggerIndexTransformer(delegado)
                .transform(mock(HttpServletRequest.class), original,
                        mock(ResourceTransformerChain.class));

        String contenido = leer(resultado);
        assertThat(contenido).contains(CsrfHeaderSwaggerIndexTransformer.MARCA);
        assertThat(contenido).contains("X-Requested-With");
        assertThat(contenido.indexOf(CsrfHeaderSwaggerIndexTransformer.MARCA))
                .isLessThan(contenido.lastIndexOf("</body>"));
    }

    @Test
    @DisplayName("No duplica el script si ya fue inyectado")
    void noDuplicaElScript() throws Exception {
        Resource original = html("index.html",
                INDEX.replace("</body>", CsrfHeaderSwaggerIndexTransformer.SCRIPT + "</body>"));
        when(delegado.transform(any(), any(), any())).thenReturn(original);

        Resource resultado = new CsrfHeaderSwaggerIndexTransformer(delegado)
                .transform(mock(HttpServletRequest.class), original,
                        mock(ResourceTransformerChain.class));

        assertThat(resultado).isSameAs(original);
    }

    @Test
    @DisplayName("Los recursos que no son HTML pasan intactos")
    void recursosNoHtmlPasanIntactos() throws Exception {
        Resource original = html("swagger-ui.css", "body {}");
        when(delegado.transform(any(), any(), any())).thenReturn(original);

        Resource resultado = new CsrfHeaderSwaggerIndexTransformer(delegado)
                .transform(mock(HttpServletRequest.class), original,
                        mock(ResourceTransformerChain.class));

        assertThat(resultado).isSameAs(original);
    }

    @Test
    @DisplayName("Un HTML que no es el index de Swagger no se modifica")
    void htmlNoSwaggerNoSeModifica() throws Exception {
        Resource original = html("index.html", "<html><body><p>Otra pagina</p></body></html>");
        when(delegado.transform(any(), any(), any())).thenReturn(original);

        Resource resultado = new CsrfHeaderSwaggerIndexTransformer(delegado)
                .transform(mock(HttpServletRequest.class), original,
                        mock(ResourceTransformerChain.class));

        assertThat(resultado).isSameAs(original);
    }

    /**
     * {@code TransformedResource} consulta {@code lastModified()} del original,
     * que un {@code ByteArrayResource} no puede resolver; un archivo real sí.
     */
    @TempDir
    Path tempDir;

    private Resource html(String filename, String contenido) throws Exception {
        Path archivo = tempDir.resolve(filename);
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return new FileSystemResource(archivo);
    }

    private String leer(Resource resource) throws Exception {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
