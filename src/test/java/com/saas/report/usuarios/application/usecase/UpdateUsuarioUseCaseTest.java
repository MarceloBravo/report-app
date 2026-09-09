package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
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

    @Test
    void actualizaNombreRolEstadoYPassword() {
        Usuario usuario = usuario();
        UpdateUsuarioRequest request = new UpdateUsuarioRequest("Bety", null, "nueva-secreta", UsuarioRol.ADMIN, false);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("nuevo-hash");

        updateUsuarioUseCase.execute(usuario.getUsuarioId(), request);

        assertThat(usuario.getNombre()).isEqualTo("Bety");
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.getActivo()).isFalse();
        assertThat(usuario.getPassword()).isEqualTo("nuevo-hash");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rechazaEmailDeOtroUsuario() {
        Usuario usuario = usuario();
        Usuario otro = new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Bety", "bety@empresa.com",
                "hash", UsuarioRol.VIEWER, true);
        UpdateUsuarioRequest request = new UpdateUsuarioRequest(null, "bety@empresa.com", null, null, null);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("bety@empresa.com")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> updateUsuarioUseCase.execute(usuario.getUsuarioId(), request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void conservaElMismoEmail() {
        Usuario usuario = usuario();
        UpdateUsuarioRequest request = new UpdateUsuarioRequest(null, "ana@empresa.com", null, null, null);

        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("ana@empresa.com")).thenReturn(Optional.of(usuario));

        updateUsuarioUseCase.execute(usuario.getUsuarioId(), request);

        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void lanzaExcepcionSiElUsuarioNoExiste() {
        when(usuarioRepository.findById(any(UsuarioUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUsuarioUseCase.execute(UsuarioUUID.generate(), new UpdateUsuarioRequest(
                null, null, null, UsuarioRol.ADMIN, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}