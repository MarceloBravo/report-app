package com.saas.report.auth.presentation;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.RefreshTokenRequest;
import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.application.usecase.LoginUseCase;
import com.saas.report.auth.application.usecase.LogoutUseCase;
import com.saas.report.auth.application.usecase.RefreshTokenUseCase;
import com.saas.report.auth.application.usecase.RegisterUseCase;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.config.exception.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RegisterUseCase registerUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(loginUseCase, registerUseCase,
                        refreshTokenUseCase, logoutUseCase, new AuthCookieService(false, "Lax")))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginDevuelveCookiesHttpOnlySinTokensEnElCuerpo() throws Exception {
        when(loginUseCase.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("abc", "def", "Bearer", 900_000, 604_800_000));

        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String access = setCookie(resultado, "access_token=");
        assertThat(access).contains("HttpOnly", "Path=/", "Max-Age=900", "SameSite=Lax")
                .doesNotContain("Secure");
        String refresh = setCookie(resultado, "refresh_token=");
        assertThat(refresh).contains("HttpOnly", "Path=/auth", "Max-Age=604800", "SameSite=Lax");
    }

    @Test
    void loginConRawDevuelveLosTokensEnElCuerpo() throws Exception {
        when(loginUseCase.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("abc", "def", "Bearer", 900_000, 604_800_000));

        mockMvc.perform(post("/auth/login?raw=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("abc"))
                .andExpect(jsonPath("$.refreshToken").value("def"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=abc")));
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        when(loginUseCase.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registroDevuelve201YUsaRolViewer() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthPrincipal("admin@empresa.com", "u1", "t1", "ADMIN"), null,
                java.util.List.of()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"a@b.com\",\"password\":\"12345678\","
                                + "\"tenantId\":\"" + java.util.UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated());

        verify(registerUseCase).registrar(any(RegisterRequest.class), eq(UsuarioRol.VIEWER),
                any(AuthPrincipal.class));
    }

    @Test
    void adminRegistryUsaRolDelRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthPrincipal("root@saas.com", "u0", null, "PLATFORM_ADMIN"), null,
                java.util.List.of()));

        mockMvc.perform(post("/auth/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pam\",\"email\":\"pam@saas.com\",\"password\":\"12345678\","
                                + "\"rol\":\"PLATFORM_USER\"}"))
                .andExpect(status().isCreated());

        verify(registerUseCase).registrar(any(RegisterRequest.class), eq(UsuarioRol.PLATFORM_USER),
                any(AuthPrincipal.class));
    }

    @Test
    void refreshDesdeCookieRotaLasCookiesSinExponerTokens() throws Exception {
        when(refreshTokenUseCase.refrescar("rt")).thenReturn(new TokenResponse("a", "b", "Bearer", 900_000, 604_800_000));

        MvcResult resultado = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieService.REFRESH_COOKIE, "rt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        verify(refreshTokenUseCase).refrescar("rt");
        assertThat(setCookie(resultado, "access_token=")).contains("HttpOnly", "Max-Age=900");
        assertThat(setCookie(resultado, "refresh_token=")).contains("HttpOnly", "Path=/auth");
    }

    @Test
    void refreshConCuerpoSigueFuncionandoParaClientesSinCookies() throws Exception {
        when(refreshTokenUseCase.refrescar("rt")).thenReturn(new TokenResponse("a", "b", "Bearer", 900_000, 604_800_000));

        MvcResult resultado = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"rt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        verify(refreshTokenUseCase).refrescar("rt");
        assertThat(setCookie(resultado, "refresh_token=")).contains("HttpOnly");
    }

    @Test
    void logoutConHeaderRevocaYBorraLasCookies() throws Exception {
        mockMvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer token-x"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(logoutUseCase).cerrarSesion("token-x");
    }

    @Test
    void logoutConCookieRevocaElTokenYBorraLasCookies() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(AuthCookieService.ACCESS_COOKIE, "token-x")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(logoutUseCase).cerrarSesion("token-x");
    }

    private String setCookie(MvcResult resultado, String prefijo) {
        return resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(valor -> valor.startsWith(prefijo))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Falta el Set-Cookie " + prefijo));
    }

    @Test
    void meDevuelveElPrincipalAutenticado() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthPrincipal("a@b.com", "u1", "t1", "ADMIN"), null, java.util.List.of()));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void meDevuelveTenantIdNuloParaUsuarioDePlataforma() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthPrincipal("admin@plataforma.com", "u2", null, "PLATFORM_ADMIN"),
                null, java.util.List.of()));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@plataforma.com"))
                .andExpect(jsonPath("$.tenantId").value(nullValue()));
    }
}