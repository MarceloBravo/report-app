package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsuariosUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private ListUsuariosUseCase listUsuariosUseCase;

    @BeforeEach
    void setUp() {
        listUsuariosUseCase = new ListUsuariosUseCase(usuarioRepository);
    }

    private Usuario usuario(String email) {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", email,
                "hash", UsuarioRol.VIEWER, true);
    }

    @Test
    void listaTodosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario("a@empresa.com"), usuario("b@empresa.com")));

        assertThat(listUsuariosUseCase.listar(null, null)).hasSize(2);
    }

    @Test
    void filtraPorRol() {
        when(usuarioRepository.findByRol(UsuarioRol.ADMIN)).thenReturn(List.of(usuario("a@empresa.com")));

        assertThat(listUsuariosUseCase.listar(UsuarioRol.ADMIN, null)).hasSize(1);
    }

    @Test
    void filtraPorEstadoActivo() {
        when(usuarioRepository.findByActivo(true)).thenReturn(List.of(usuario("a@empresa.com")));

        assertThat(listUsuariosUseCase.listar(null, true)).hasSize(1);
    }
}