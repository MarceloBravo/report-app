package com.saas.report.auth.application.usecase;

import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private UsuarioTokenRepository usuarioTokenRepository;

    private LogoutUseCase logoutUseCase;

    @BeforeEach
    void setUp() {
        logoutUseCase = new LogoutUseCase(usuarioTokenRepository);
    }

    private UsuarioToken tokenActivo() {
        LocalDateTime now = LocalDateTime.now();
        AuthUser usuario = new AuthUser(UUID.randomUUID(), "ana@empresa.com", "Ana",
                "VIEWER", UUID.randomUUID());
        return new UsuarioToken(UsuarioTokenUUID.generate(), usuario,
                "access-token", "refresh-token", now, now.plusMinutes(15), now.plusDays(7), true);
    }

    @Test
    void revocaElTokenActivoDelUsuario() {
        UsuarioToken token = tokenActivo();

        when(usuarioTokenRepository.findByAccessToken("access-token")).thenReturn(Optional.of(token));

        logoutUseCase.cerrarSesion("access-token");

        assertThat(token.estaActivo()).isFalse();
        verify(usuarioTokenRepository).save(token);
    }

    @Test
    void noHaceNadaSiElTokenNoExiste() {
        when(usuarioTokenRepository.findByAccessToken("desconocido")).thenReturn(Optional.empty());

        logoutUseCase.cerrarSesion("desconocido");
    }

    @Test
    void rechazaTokenNuloOVacio() {
        assertThatThrownBy(() -> logoutUseCase.cerrarSesion(null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> logoutUseCase.cerrarSesion("  "))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}