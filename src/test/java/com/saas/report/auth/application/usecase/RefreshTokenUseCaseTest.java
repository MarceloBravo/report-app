package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.exception.TokenRevokedException;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    private static final String REFRESH = "refresh-token";

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioTokenRepository usuarioTokenRepository;

    @Mock
    private AuthTokenIssuer tokenIssuer;

    private RefreshTokenUseCase refreshTokenUseCase;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCase(tokenService, usuarioTokenRepository, tokenIssuer);
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.VIEWER, true);
    }

    private UsuarioToken tokenActivo() {
        LocalDateTime now = LocalDateTime.now();
        return new UsuarioToken(UsuarioTokenUUID.generate(), usuario(),
                "access-token", REFRESH, now, now.plusMinutes(15), now.plusDays(7), true);
    }

    @Test
    void revocaElRegistroActualYEmiteNuevosTokens() {
        Usuario usuario = usuario();
        UsuarioToken registro = new UsuarioToken(UsuarioTokenUUID.generate(), usuario,
                "access-token", REFRESH, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15),
                LocalDateTime.now().plusDays(7), true);
        TokenResponse esperado = new TokenResponse("new-access", "new-refresh", "Bearer", 1000, 2000);

        when(tokenService.esRefreshToken(REFRESH)).thenReturn(true);
        when(usuarioTokenRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(registro));
        when(tokenIssuer.emitirTokens(usuario)).thenReturn(esperado);

        TokenResponse response = refreshTokenUseCase.refrescar(REFRESH);

        assertThat(response).isEqualTo(esperado);
        assertThat(registro.estaActivo()).isFalse();
        verify(usuarioTokenRepository).save(registro);
        verify(tokenIssuer).emitirTokens(usuario);
    }

    @Test
    void rechazaRefreshTokenNuloOVacio() {
        assertThatThrownBy(() -> refreshTokenUseCase.refrescar(null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> refreshTokenUseCase.refrescar("  "))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rechazaTokenQueNoEsRefresh() {
        when(tokenService.esRefreshToken(anyString())).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenUseCase.refrescar(REFRESH))
                .isInstanceOf(TokenRevokedException.class);
    }

    @Test
    void rechazaRefreshTokenInexistente() {
        when(tokenService.esRefreshToken(REFRESH)).thenReturn(true);
        when(usuarioTokenRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.refrescar(REFRESH))
                .isInstanceOf(TokenRevokedException.class);
    }

    @Test
    void rechazaRefreshTokenInactivo() {
        LocalDateTime now = LocalDateTime.now();
        UsuarioToken inactivo = new UsuarioToken(UsuarioTokenUUID.generate(), usuario(),
                "access-token", REFRESH, now, now.plusMinutes(15), now.plusDays(7), false);

        when(tokenService.esRefreshToken(REFRESH)).thenReturn(true);
        when(usuarioTokenRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> refreshTokenUseCase.refrescar(REFRESH))
                .isInstanceOf(TokenRevokedException.class);
    }

    @Test
    void rechazaYRevocaRefreshTokenExpirado() {
        LocalDateTime now = LocalDateTime.now();
        UsuarioToken expirado = new UsuarioToken(UsuarioTokenUUID.generate(), usuario(),
                "access-token", REFRESH, now.minusDays(2), now.minusDays(1), now.minusHours(12), true);

        when(tokenService.esRefreshToken(REFRESH)).thenReturn(true);
        when(usuarioTokenRepository.findByRefreshToken(REFRESH)).thenReturn(Optional.of(expirado));

        assertThatThrownBy(() -> refreshTokenUseCase.refrescar(REFRESH))
                .isInstanceOf(TokenRevokedException.class);
        assertThat(expirado.estaActivo()).isFalse();
        verify(usuarioTokenRepository).save(expirado);
    }
}