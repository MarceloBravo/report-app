package com.saas.report.usuarios.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.usuarios.application.dto.UsuarioResponse;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
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
    void mapeaCadaUsuarioDeLaPaginaAResponse() {
        when(usuarioRepository.findAll(isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(
                        List.of(usuario("a@empresa.com"), usuario("b@empresa.com")),
                        PageRequest.of(0, 10), 2));

        Page<UsuarioResponse> response = listUsuariosUseCase.listar(null, null, 0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).email()).isEqualTo("a@empresa.com");
        assertThat(response.getContent().get(0).nombre()).isEqualTo("Ana");
    }

    @Test
    void conservaLosMetadatosDePaginacion() {
        when(usuarioRepository.findAll(isNull(), isNull(), eq(1), eq(5)))
                .thenReturn(new PageImpl<>(List.of(usuario("a@empresa.com")), PageRequest.of(1, 5), 20));

        Page<UsuarioResponse> response = listUsuariosUseCase.listar(null, null, 1, 5);

        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(20);
        assertThat(response.getTotalPages()).isEqualTo(4);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    void delegaElFiltroPorRol() {
        when(usuarioRepository.findAll(eq(UsuarioRol.ADMIN), isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(usuario("a@empresa.com")), PageRequest.of(0, 10), 1));

        assertThat(listUsuariosUseCase.listar(UsuarioRol.ADMIN, null, 0, 10).getContent()).hasSize(1);

        verify(usuarioRepository).findAll(UsuarioRol.ADMIN, null, 0, 10);
    }

    @Test
    void delegaElFiltroPorEstadoActivo() {
        when(usuarioRepository.findAll(isNull(), eq(true), any(), any()))
                .thenReturn(new PageImpl<>(List.of(usuario("a@empresa.com")), PageRequest.of(0, 10), 1));

        assertThat(listUsuariosUseCase.listar(null, true, 0, 10).getContent()).hasSize(1);

        verify(usuarioRepository).findAll(null, true, 0, 10);
    }

    @Test
    void combinaLosFiltrosDeRolYEstado() {
        when(usuarioRepository.findAll(eq(UsuarioRol.VIEWER), eq(false), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        assertThat(listUsuariosUseCase.listar(UsuarioRol.VIEWER, false, 0, 10).getContent()).isEmpty();

        verify(usuarioRepository).findAll(UsuarioRol.VIEWER, false, 0, 10);
    }

    @Test
    void delegaPageYSizeAlRepositorio() {
        when(usuarioRepository.findAll(isNull(), isNull(), eq(3), eq(25)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(3, 25), 0));

        listUsuariosUseCase.listar(null, null, 3, 25);

        verify(usuarioRepository).findAll(null, null, 3, 25);
    }
}