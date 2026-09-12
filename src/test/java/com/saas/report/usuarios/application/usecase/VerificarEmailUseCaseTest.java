package com.saas.report.usuarios.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificarEmailUseCaseTest {

    @Mock
    private EmailVerificationTokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private VerificarEmailUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new VerificarEmailUseCase(tokenService, usuarioRepository);
    }

    private Usuario usuarioNoVerificado() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Juan", "juan@correo.com",
                "hash", UsuarioRol.ADMIN, true, false);
    }

    @Test
    void verificaElEmailDelUsuario() {
        Usuario usuario = usuarioNoVerificado();
        when(tokenService.verificar("token")).thenReturn(usuario.getUsuarioId());
        when(usuarioRepository.findById(usuario.getUsuarioId())).thenReturn(Optional.of(usuario));

        useCase.verificar("token");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getEmailVerificado()).isTrue();
    }

    @Test
    void noVuelveAGuardarSiElEmailYaEstabaVerificado() {
        Usuario usuario = new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Juan", "juan@correo.com",
                "hash", UsuarioRol.ADMIN, true, true);
        when(tokenService.verificar("token")).thenReturn(usuario.getUsuarioId());
        when(usuarioRepository.findById(usuario.getUsuarioId())).thenReturn(Optional.of(usuario));

        useCase.verificar("token");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void lanzaErrorSiElUsuarioNoExiste() {
        when(tokenService.verificar("token")).thenReturn(UsuarioUUID.generate());
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.verificar("token"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}