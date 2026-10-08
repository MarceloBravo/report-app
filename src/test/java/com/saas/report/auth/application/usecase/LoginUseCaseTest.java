package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.usuario.UsuarioAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioAuthRepository usuarioAuthRepository;

    @Mock
    private AuthTokenIssuer tokenIssuer;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(authenticationManager, usuarioAuthRepository, tokenIssuer);
    }

    private AuthUser usuario() {
        return new AuthUser(UUID.randomUUID(), "ana@empresa.com", "Ana", "VIEWER", UUID.randomUUID());
    }

    @Test
    void autenticaYEmiteTokens() {
        AuthUser usuario = usuario();
        TokenResponse esperado = new TokenResponse("access", "refresh", "Bearer", 1000, 2000);
        LoginRequest request = new LoginRequest("ana@empresa.com", "secret123");

        when(usuarioAuthRepository.buscarPorEmail(request.email())).thenReturn(Optional.of(usuario));
        when(tokenIssuer.emitirTokens(usuario)).thenReturn(esperado);

        TokenResponse response = loginUseCase.login(request);

        assertThat(response).isEqualTo(esperado);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(request.email());
        assertThat(captor.getValue().getCredentials()).isEqualTo(request.password());
        verify(tokenIssuer).emitirTokens(usuario);
    }

    @Test
    void lanzaExcepcionSiElUsuarioNoExiste() {
        LoginRequest request = new LoginRequest("ana@empresa.com", "secret123");

        when(usuarioAuthRepository.buscarPorEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}