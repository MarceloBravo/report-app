package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.support.AbstractSecurityMvcTest;
import com.saas.report.support.CsrfProbeController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz del {@link CustomHeaderCsrfFilter} contra la SecurityConfig real.
 *
 * <p>El contrato: la cabecera {@code X-Requested-With: XMLHttpRequest} solo se
 * exige cuando hay cookies de sesión (credenciales ambientales que un ataque
 * CSRF puede explotar) o en {@code /auth/login} (login CSRF). Los clientes sin
 * cookies, como curl con {@code Authorization: Bearer}, no cambian.
 */
@WebMvcTest(controllers = CsrfProbeController.class)
class CustomHeaderCsrfFilterTest extends AbstractSecurityMvcTest {

    private static final String CABECERA = CustomHeaderCsrfFilter.HEADER;
    private static final String VALOR = CustomHeaderCsrfFilter.VALOR;

    @Test
    @DisplayName("POST con cookie de sesión y sin la cabecera recibe 403")
    void postConCookieSinCabeceraEsRechazado() throws Exception {
        mockMvc.perform(autenticadoPorCookie(UsuarioRol.ADMIN, post(CsrfProbeController.RUTA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString(CABECERA)));
    }

    @Test
    @DisplayName("POST con cookie de sesión y la cabecera correcta pasa")
    void postConCookieYCabeceraCorrectaPasa() throws Exception {
        mockMvc.perform(autenticadoPorCookie(UsuarioRol.ADMIN, post(CsrfProbeController.RUTA))
                        .header(CABECERA, VALOR))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST con cookie y la cabecera con otro valor recibe 403")
    void postConValorDeCabeceraIncorrectoEsRechazado() throws Exception {
        mockMvc.perform(autenticadoPorCookie(UsuarioRol.ADMIN, post(CsrfProbeController.RUTA))
                        .header(CABECERA, "otro-valor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST sin cookies con Authorization: Bearer pasa sin la cabecera")
    void postConBearerSinCookieNoExigeCabecera() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.ADMIN, post(CsrfProbeController.RUTA)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST sin cookies y sin credenciales no recibe 403 de CSRF")
    void postAnonimoSinCookieNoEsRechazadoPorCsrf() throws Exception {
        mockMvc.perform(post(CsrfProbeController.RUTA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET con cookie de sesión pasa sin la cabecera")
    void getConCookieNoExigeCabecera() throws Exception {
        mockMvc.perform(autenticadoPorCookie(UsuarioRol.ADMIN, get(CsrfProbeController.RUTA)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/login siempre exige la cabecera, aunque no haya cookies")
    void loginSinCabeceraEsRechazado() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString(CABECERA)));
    }

    @Test
    @DisplayName("POST /auth/login con la cabecera supera el filtro CSRF")
    void loginConCabeceraSuperaElFiltro() throws Exception {
        // AuthController no forma parte de este slice: si el filtro deja pasar,
        // la request llega al DispatcherServlet y resuelve 404 (no 403).
        mockMvc.perform(post("/auth/login").header(CABECERA, VALOR))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /auth/refresh con cookie de sesión exige la cabecera")
    void refreshConCookieSinCabeceraEsRechazado() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieService.REFRESH_COOKIE, "refresh-de-prueba")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El preflight OPTIONS nunca exige la cabecera")
    void preflightNoExigeCabecera() throws Exception {
        mockMvc.perform(options(CsrfProbeController.RUTA)
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .cookie(new Cookie(AuthCookieService.ACCESS_COOKIE, ACCESS_TOKEN)))
                .andExpect(status().isOk());
    }
}
