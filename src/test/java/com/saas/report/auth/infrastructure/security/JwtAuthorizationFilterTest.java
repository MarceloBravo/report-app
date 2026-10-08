package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba el {@code JwtAuthorizationFilter} contra el token que viaja en la
 * cookie {@code access_token}, que es como se autentica el frontend Angular.
 * La cabecera {@code Authorization} sigue siendo el primer camino.
 */
class JwtAuthorizationFilterTest {

    private static final String TOKEN = "jwt-de-prueba";

    private TokenService tokenService;
    private UsuarioTokenRepository usuarioTokenRepository;
    private JwtAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        usuarioTokenRepository = mock(UsuarioTokenRepository.class);
        filter = new JwtAuthorizationFilter(tokenService, usuarioTokenRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Autentica con la cookie HttpOnly access_token")
    void autenticaConCookieDeAcceso() throws Exception {
        dadoTokenValidoEnRepositorio(TOKEN, true, LocalDateTime.now().plusMinutes(10));

        filter.doFilter(peticionConCookie(TOKEN), new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(((AuthPrincipal) authentication.getPrincipal()).email()).isEqualTo("prueba-admin@test.com");
    }

    @Test
    @DisplayName("Sin cabecera ni cookie no autentica")
    void noAutenticaSinCredenciales() throws Exception {
        filter.doFilter(peticionSinCredenciales(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("La cabecera Authorization tiene prioridad sobre la cookie")
    void laCabeceraTienePrioridadSobreLaCookie() throws Exception {
        dadoTokenValidoEnRepositorio("token-de-la-cabecera", true, LocalDateTime.now().plusMinutes(10));

        var request = peticionConCookie("token-de-la-cabecera");
        request.addHeader("Authorization", "Bearer token-de-la-cabecera");
        request.setCookies(new Cookie(AuthCookieService.ACCESS_COOKIE, "token-distinto"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(usuarioTokenRepository).findByAccessToken("token-de-la-cabecera");
    }

    @Test
    @DisplayName("Una cookie con token revocado no autentica")
    void noAutenticaConRegistroInactivo() throws Exception {
        dadoTokenValidoEnRepositorio(TOKEN, false, LocalDateTime.now().plusMinutes(10));

        filter.doFilter(peticionConCookie(TOKEN), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private void dadoTokenValidoEnRepositorio(String token, boolean activo, LocalDateTime expiraEn) {
        when(tokenService.esTokenValido(token)).thenReturn(true);
        when(usuarioTokenRepository.findByAccessToken(token))
                .thenReturn(Optional.of(usuarioToken(token, activo, expiraEn)));
    }

    private MockHttpServletRequest peticionConCookie(String token) {
        MockHttpServletRequest request = peticionSinCredenciales();
        request.setCookies(new Cookie(AuthCookieService.ACCESS_COOKIE, token));
        return request;
    }

    private MockHttpServletRequest peticionSinCredenciales() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/usuarios");
        request.setServletPath("/admin/usuarios");
        return request;
    }

    private UsuarioToken usuarioToken(String token, boolean activo, LocalDateTime expiraEn) {
        LocalDateTime creacion = LocalDateTime.now().minusMinutes(5);
        AuthUser usuario = new AuthUser(
                UUID.randomUUID(),
                "prueba-admin@test.com",
                "Usuario de Prueba",
                UsuarioRol.ADMIN.name(),
                UUID.randomUUID());
        return new UsuarioToken(
                UsuarioTokenUUID.generate(),
                usuario,
                token,
                "refresh-" + token,
                creacion,
                expiraEn,
                expiraEn.plusDays(6),
                activo);
    }
}
