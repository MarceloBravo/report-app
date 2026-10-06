package com.saas.report.auth.infrastructure.config;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.application.usecase.LoginUseCase;
import com.saas.report.auth.application.usecase.LogoutUseCase;
import com.saas.report.auth.application.usecase.RefreshTokenUseCase;
import com.saas.report.auth.application.usecase.RegisterUseCase;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.infrastructure.security.RestAccessDeniedHandler;
import com.saas.report.auth.infrastructure.security.RestAuthenticationEntryPoint;
import com.saas.report.auth.presentation.AuthController;
import com.saas.report.onboarding.infrastructure.security.SelfServeRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que la SecurityConfig real resuelva el flujo CORS que usa el
 * frontend Angular en {@code http://localhost:4200}: preflight aprobado,
 * rechazo de orígenes no listados y cabeceras CORS también en respuestas 401.
 *
 * <p>La propiedad se declara con espacios tras la coma a propósito: cubre que
 * el parseo de {@code app.cors.allowed-origins} recorte cada origen.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        AuthCookieService.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200, http://localhost:5173")
class CorsSecurityTest {

    private static final String ORIGEN_ANGULAR = "http://localhost:4200";

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioTokenRepository usuarioTokenRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private SelfServeRateLimiter selfServeRateLimiter;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("El preflight del frontend en localhost:4200 es aceptado")
    void preflightDesdeAngularEsAceptado() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, ORIGEN_ANGULAR)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN_ANGULAR))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("Un origen que no está en la lista no recibe Access-Control-Allow-Origin")
    void preflightDesdeOrigenNoPermitidoNoRecibeCabeceras() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("Las respuestas 401 también llevan cabeceras CORS para el navegador")
    void respuestasDeErrorLlevanCabecerasCors() throws Exception {
        mockMvc.perform(get("/auth/me").header(HttpHeaders.ORIGIN, ORIGEN_ANGULAR))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN_ANGULAR));
    }
}
