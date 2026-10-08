package com.saas.report.usuarios.application.usecase;

import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
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
        useCase = new VerificarEmailUseCase(usuarioRepository, tokenService);
    }

    private Usuario usuario(Boolean emailVerificado) {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Juan", "juan@correo.com",
                "hash", UsuarioRol.ADMIN, true, emailVerificado);
    }

    @Test
    void verificaElEmailDelUsuario() {
        Usuario usuario = usuario(false);
        when(tokenService.esValido(usuario.getUsuarioId().getValue(), "token")).thenReturn(true);
        when(usuarioRepository.findById(usuario.getUsuarioId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.ejecutar(usuario.getUsuarioId(), "token");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getEmailVerificado()).isTrue();
    }

    @Test
    void verificaAPartirDelToken() {
        Usuario usuario = usuario(false);
        when(tokenService.extraerUsuarioId("token")).thenReturn(usuario.getUsuarioId());
        when(tokenService.esValido(usuario.getUsuarioId().getValue(), "token")).thenReturn(true);
        when(usuarioRepository.findById(usuario.getUsuarioId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.verificar("token");

        verify(usuarioRepository).save(any());
    }

    @Test
    void rechazaTokenInvalido() {
        UsuarioUUID id = UsuarioUUID.generate();
        when(tokenService.esValido(id.getValue(), "token")).thenReturn(false);

        assertThatThrownBy(() -> useCase.ejecutar(id, "token"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void lanzaErrorSiElUsuarioNoExiste() {
        UsuarioUUID id = UsuarioUUID.generate();
        when(tokenService.esValido(id.getValue(), "token")).thenReturn(true);
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(id, "token"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}