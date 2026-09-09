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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private DeleteUsuarioUseCase deleteUsuarioUseCase;

    @BeforeEach
    void setUp() {
        deleteUsuarioUseCase = new DeleteUsuarioUseCase(usuarioRepository);
    }

    @Test
    void eliminaElUsuarioExistente() {
        Usuario usuario = new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.VIEWER, true);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));

        deleteUsuarioUseCase.execute(usuario.getUsuarioId());

        verify(usuarioRepository).delete(eq(usuario.getUsuarioId()));
    }

    @Test
    void lanzaExcepcionSiElUsuarioNoExiste() {
        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUsuarioUseCase.execute(UsuarioUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}