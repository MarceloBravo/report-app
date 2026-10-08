package com.saas.report.auth.application;

import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.usuario.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenIssuerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioTokenRepository usuarioTokenRepository;

    private AuthTokenIssuer tokenIssuer;

    @BeforeEach
    void setUp() {
        tokenIssuer = new AuthTokenIssuer(tokenService, usuarioTokenRepository);
    }

    @Test
    void emiteTokensYPersisteElRegistro() {
        AuthUser usuario = new AuthUser(UUID.randomUUID(), "ana@empresa.com", "Ana",
                "VIEWER", UUID.randomUUID());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExp = now.plusMinutes(15);
        LocalDateTime refreshExp = now.plusDays(7);

        when(tokenService.generateAccessToken(usuario)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(tokenService.extraerFechaCreacion("access-token")).thenReturn(now);
        when(tokenService.extraerExpiracion("access-token")).thenReturn(accessExp);
        when(tokenService.extraerExpiracion("refresh-token")).thenReturn(refreshExp);

        TokenResponse response = tokenIssuer.emitirTokens(usuario);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(java.time.Duration.between(now, accessExp).toMillis());
        assertThat(response.refreshExpiresIn()).isEqualTo(java.time.Duration.between(now, refreshExp).toMillis());

        ArgumentCaptor<UsuarioToken> captor = ArgumentCaptor.forClass(UsuarioToken.class);
        verify(usuarioTokenRepository).save(captor.capture());
        UsuarioToken registro = captor.getValue();
        assertThat(registro.getAccessToken()).isEqualTo("access-token");
        assertThat(registro.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(registro.getUsuario()).isEqualTo(usuario);
        assertThat(registro.estaActivo()).isTrue();
    }
}