package com.saas.report.auth.infrastructure.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Sustituye el {@code SwaggerIndexTransformer} por defecto de springdoc (bean
 * declarado con {@code @ConditionalOnMissingBean}) por una versión que además
 * inyecta la cabecera CSRF en las requests de la UI.
 *
 * <p>Activo solo en el perfil {@code dev} con la UI habilitada, que son las
 * mismas condiciones con las que springdoc registra sus propios beans.
 */
@Configuration
@Profile("dev")
public class SwaggerCsrfConfig {

    @Bean
    @ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true",
            matchIfMissing = true)
    public SwaggerIndexTransformer swaggerIndexTransformerConCabeceraCsrf(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        return new CsrfHeaderSwaggerIndexTransformer(
                new SwaggerIndexPageTransformer(swaggerUiConfigProperties, swaggerUiOAuthProperties,
                        swaggerWelcomeCommon, objectMapperProvider));
    }
}
