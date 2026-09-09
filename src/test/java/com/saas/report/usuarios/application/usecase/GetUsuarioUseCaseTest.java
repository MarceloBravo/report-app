package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private GetUsuarioUseCase getUsuarioUseCase;

    @BeforeEach
    void setUp() {
        getUsuarioUseCase = new GetUsuarioUseCase(usuarioRepository);
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.ADMIN, true);
    }

    @Test
    void obtieneElUsuario() {
        Usuario usuario = usuario();

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));

        assertThat(getUsuarioUseCase.execute(usuario.getUsuarioId())).isEqualTo(usuario);
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUsuarioUseCase.execute(UsuarioUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}