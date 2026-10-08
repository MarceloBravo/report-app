package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.CreadorPor;
import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UpdateUsuarioUseCase updateUsuarioUseCase;

    @BeforeEach
    void setUp() {
        updateUsuarioUseCase = new UpdateUsuarioUseCase(usuarioRepository, passwordEncoder);
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.VIEWER, true);
    }

    private CreadorPor creador() {
        return new CreadorPor("root@saas.com", UsuarioRol.PLATFORM_ADMIN.name());
    }

    @Test
    void actualizaNombreRolEstadoYPassword() {
        Usuario usuario = usuario();
        UpdateUsuarioRequest request = new UpdateUsuarioRequest(
                "Bety", null, null, "nueva-secreta", UsuarioRol.ADMIN, false);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        updateUsuarioUseCase.execute(usuario.getUsuarioId(), request, creador());

        assertThat(usuario.getNombre()).isEqualTo("Bety");
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.getActivo()).isFalse();
        assertThat(usuario.getPassword()).isEqualTo("encoded-hash");
        verify(usuarioRepository).save(any());
    }

    @Test
    void rechazaEmailDeOtroUsuario() {
        Usuario usuario = usuario();
        UpdateUsuarioRequest request = new UpdateUsuarioRequest(
                null, null, "otro@empresa.com", null, null, null);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("otro@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> updateUsuarioUseCase.execute(usuario.getUsuarioId(), request, creador()))
                .isInstanceOf(ForbiddenException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void conservaElMismoEmail() {
        Usuario usuario = usuario();
        UpdateUsuarioRequest request = new UpdateUsuarioRequest(
                null, null, "ana@empresa.com", null, null, null);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        updateUsuarioUseCase.execute(usuario.getUsuarioId(), request, creador());

        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        verify(usuarioRepository).save(any());
    }

    @Test
    void lanzaExcepcionSiElUsuarioNoExiste() {
        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.empty());

        UpdateUsuarioRequest request = new UpdateUsuarioRequest(null, null, null, null, null, null);
        assertThatThrownBy(() -> updateUsuarioUseCase.execute(UsuarioUUID.generate(), request, creador()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}