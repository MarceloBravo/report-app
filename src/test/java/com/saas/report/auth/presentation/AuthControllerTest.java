package com.saas.report.auth.presentation;

import com.saas.report.auth.application.AuthPrincipal;
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
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        refreshTokenUseCase, logoutUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginDevuelve200ConTokens() throws Exception {
        when(loginUseCase.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("abc", "def", "Bearer", 900, 604800));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("abc"));
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
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"a@b.com\",\"password\":\"12345678\","
                                + "\"tenantId\":\"" + java.util.UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated());

        verify(registerUseCase).registrar(any(RegisterRequest.class), eq(UsuarioRol.VIEWER));
    }

    @Test
    void refreshDevuelve200() throws Exception {
        when(refreshTokenUseCase.refrescar("rt")).thenReturn(new TokenResponse("a", "b", "Bearer", 900, 604800));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"rt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("b"));
    }

    @Test
    void logoutDevuelve204YRevocaElToken() throws Exception {
        mockMvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer token-x"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).cerrarSesion("token-x");
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
}