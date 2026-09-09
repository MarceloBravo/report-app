package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUsuarioUseCase createUsuarioUseCase;

    @BeforeEach
    void setUp() {
        createUsuarioUseCase = new CreateUsuarioUseCase(usuarioRepository, passwordEncoder);
    }

    private UsuarioRequest request() {
        return new UsuarioRequest(UUID.randomUUID(), "Ana", "ana@empresa.com", "secreto123", null, true);
    }

    @Test
    void creaUsuarioConPasswordEncriptadaYRolPorDefecto() {
        UsuarioRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Usuario creado = createUsuarioUseCase.execute(request);

        assertThat(creado.getNombre()).isEqualTo("Ana");
        assertThat(creado.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(creado.getPassword()).isEqualTo("encoded-hash");
        assertThat(creado.getRol()).isEqualTo(UsuarioRol.VIEWER);
        assertThat(creado.getActivo()).isTrue();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.VIEWER);
    }

    @Test
    void usaElRolYEstadoSolicitados() {
        UsuarioRequest request = new UsuarioRequest(UUID.randomUUID(), "Ana", "ana@empresa.com",
                "secreto123", UsuarioRol.ADMIN, false);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        createUsuarioUseCase.execute(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(captor.getValue().getActivo()).isFalse();
    }

    @Test
    void rechazaEmailYaRegistrado() {
        UsuarioRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> createUsuarioUseCase.execute(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }
}