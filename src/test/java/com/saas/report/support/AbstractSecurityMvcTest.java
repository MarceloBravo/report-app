package com.saas.report.support;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.auth.infrastructure.config.SecurityConfig;
import com.saas.report.auth.infrastructure.security.RestAccessDeniedHandler;
import com.saas.report.auth.infrastructure.security.RestAuthenticationEntryPoint;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.onboarding.infrastructure.security.SelfServeRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Base común para probar la <b>cadena de seguridad real</b> (SecurityConfig +
 * JwtAuthorizationFilter + RestAuthenticationEntryPoint/RestAccessDeniedHandler)
 * contra rutas HTTP, sin base de datos ni Docker.
 *
 * <p>Se apoya en un slice {@code @WebMvcTest} con la configuración real importada,
 * no en una cadena reconstruida a mano: así los tests detectan errores de
 * cableado que los tests de controller con MockMvc aislado no ven (orden de los
 * matchers, roles mal escritos, reglas que nunca se aplican, filtro no
 * registrado...).
 *
 * <p>La autenticación se hace por el camino real: se da de alta un
 * {@link UsuarioToken} en el repositorio y se envía la cabecera
 * {@code Authorization: Bearer}. El filtro resuelve el {@link Usuario} y arma el
 * {@code AuthPrincipal} con el tenant, igual que en producción.
 *
 * <p>Uso típico:
 * <pre>{@code
 * @WebMvcTest(controllers = MiController.class)
 * class MiSecurityTest extends AbstractSecurityMvcTest {
 *
 *     @Test
 *     void viewerNoPuedeVer() throws Exception {
 *         mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, get("/mia")))
 *                .andExpect(status().isForbidden());
 *     }
 * }
 * }</pre>
 */
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
public abstract class AbstractSecurityMvcTest {

    /** Token fijo: los dobles del repositorio responden siempre a este valor. */
    protected static final String ACCESS_TOKEN = "jwt-de-prueba";

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * TokenService y el repositorio de tokens son adaptadores de infraestructura
     * y no se cargan en el slice; se sustituyen por dobles.
     */
    @MockitoBean
    protected TokenService tokenService;

    @MockitoBean
    protected UsuarioTokenRepository usuarioTokenRepository;

    /**
     * El UserDetailsService real consulta la base de datos. El slice no lo carga,
     * pero SecurityConfig publica un AuthenticationManager que lo necesita.
     */
    @MockitoBean
    protected UserDetailsService userDetailsService;

    /**
     * SelfServeRateLimitFilter sí se carga (es un Filter) y necesita su limiter,
     * que es un bean normal fuera del alcance del slice.
     */
    @MockitoBean
    protected SelfServeRateLimiter selfServeRateLimiter;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void permitirSelfServe() {
        lenient().when(selfServeRateLimiter.intentarConsumir(anyString())).thenReturn(true);
    }

    /** Pide la ruta con un JWT válido, activo y sin expirar para el rol indicado. */
    protected MockHttpServletRequestBuilder autenticadoComo(UsuarioRol rol,
                                                            MockHttpServletRequestBuilder request) {
        return conTokenDe(usuarioCon(rol), LocalDateTime.now().plusHours(1), true, request);
    }

    /**
     * Pide la ruta autenticándose por la cookie {@code access_token}, igual que
     * el frontend Angular en el navegador. No añade la cabecera
     * {@code X-Requested-With}, salvo que el test la agregue a mano.
     */
    protected MockHttpServletRequestBuilder autenticadoPorCookie(UsuarioRol rol,
                                                                 MockHttpServletRequestBuilder request) {
        prepararToken(usuarioCon(rol), LocalDateTime.now().plusHours(1), true);
        return request.cookie(
                new Cookie(AuthCookieService.ACCESS_COOKIE, ACCESS_TOKEN));
    }

    /** Pide la ruta con un JWT que el TokenService no reconoce. */
    protected MockHttpServletRequestBuilder conTokenInvalido(MockHttpServletRequestBuilder request) {
        lenient().when(tokenService.esTokenValido(anyString())).thenReturn(false);
        lenient().when(usuarioTokenRepository.findByAccessToken(anyString())).thenReturn(Optional.empty());
        return conCabecera(request);
    }

    /** Pide la ruta con un JWT cuyo access token ya expiró. */
    protected MockHttpServletRequestBuilder conTokenExpirado(MockHttpServletRequestBuilder request) {
        return conTokenDe(usuarioCon(UsuarioRol.ADMIN), LocalDateTime.now().minusHours(1), true, request);
    }

    /** Pide la ruta con un JWT revocado (el registro está inactivo). */
    protected MockHttpServletRequestBuilder conTokenRevocado(MockHttpServletRequestBuilder request) {
        return conTokenDe(usuarioCon(UsuarioRol.ADMIN), LocalDateTime.now().plusHours(1), false, request);
    }

    /**
     * Pide la ruta con un JWT válido a nombre del usuario indicado. Permite
     * montar principals imposibles con el constructor de {@link Usuario}, como
     * un ADMIN de tenant sin tenant asociado.
     */
    protected MockHttpServletRequestBuilder conTokenDe(AuthUser usuario,
                                                      MockHttpServletRequestBuilder request) {
        return conTokenDe(usuario, LocalDateTime.now().plusHours(1), true, request);
    }

    private MockHttpServletRequestBuilder conTokenDe(AuthUser usuario,
                                                      LocalDateTime accessExpiraEn,
                                                      boolean activo,
                                                      MockHttpServletRequestBuilder request) {
        prepararToken(usuario, accessExpiraEn, activo);
        return conCabecera(request);
    }

    private void prepararToken(AuthUser usuario, LocalDateTime accessExpiraEn, boolean activo) {
        lenient().when(tokenService.esTokenValido(ACCESS_TOKEN)).thenReturn(true);
        lenient().when(usuarioTokenRepository.findByAccessToken(ACCESS_TOKEN))
                .thenReturn(Optional.of(registroDe(usuario, accessExpiraEn, activo)));
    }

    private MockHttpServletRequestBuilder conCabecera(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + ACCESS_TOKEN);
    }

    /** Usuario del rol pedido; los roles de tenant reciben un tenant y los de plataforma, ninguno. */
    protected AuthUser usuarioCon(UsuarioRol rol) {
        return new AuthUser(
                UUID.randomUUID(),
                "prueba-" + rol.name().toLowerCase() + "@test.com",
                "Usuario de Prueba",
                rol.name(),
                rol.esRolTenant() ? UUID.randomUUID() : null);
    }

    private UsuarioToken registroDe(AuthUser usuario, LocalDateTime accessExpiraEn, boolean activo) {
        LocalDateTime ahora = LocalDateTime.now();
        // UsuarioToken exige que la expiración no sea anterior a la creación.
        LocalDateTime creacion = accessExpiraEn.isBefore(ahora)
                ? accessExpiraEn.minusHours(1)
                : ahora.minusMinutes(5);
        return new UsuarioToken(
                UsuarioTokenUUID.generate(),
                usuario,
                ACCESS_TOKEN,
                "refresh-" + ACCESS_TOKEN,
                creacion,
                accessExpiraEn,
                accessExpiraEn.plusHours(8),
                activo);
    }
}