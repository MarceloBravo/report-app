package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.user.UsuarioRepository;
import com.saas.report.auth.domain.user.UsuarioRol;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthTokenIssuer tokenIssuer;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(authenticationManager, usuarioRepository, tokenIssuer);
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.VIEWER, true);
    }

    @Test
    void autenticaYEmiteTokens() {
        Usuario usuario = usuario();
        TokenResponse esperado = new TokenResponse("access", "refresh", "Bearer", 1000, 2000);
        LoginRequest request = new LoginRequest("ana@empresa.com", "secret123");

        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.of(usuario));
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

        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}