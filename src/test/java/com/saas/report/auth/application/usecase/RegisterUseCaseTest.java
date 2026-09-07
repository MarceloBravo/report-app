package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.domain.exception.EmailAlreadyExistsException;
import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.user.UsuarioRepository;
import com.saas.report.auth.domain.user.UsuarioRol;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUseCase(usuarioRepository, tenantRepository, passwordEncoder);
    }

    private RegisterRequest request() {
        return new RegisterRequest("Ana", "ana@empresa.com", "secreto123", UUID.randomUUID(), null);
    }

    private Tenant tenant(TenantEstado estado) {
        return new Tenant(TenantUUID.generate(), "Empresa", "empresa", estado);
    }

    @Test
    void registraUsuarioConPasswordEncriptada() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.VIEWER);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getNombre()).isEqualTo("Ana");
        assertThat(guardado.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(guardado.getPassword()).isEqualTo("encoded-hash");
        assertThat(guardado.getRol()).isEqualTo(UsuarioRol.VIEWER);
        assertThat(guardado.getActivo()).isTrue();
        assertThat(guardado.getTenantId()).isEqualTo(tenant.getTenantId());
    }

    @Test
    void registraUsuarioConRolAdmin() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.ADMIN);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.ADMIN);
    }

    @Test
    void rechazaEmailYaRegistrado() {
        RegisterRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void rechazaTenantInexistente() {
        RegisterRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaTenantInactivo() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.SUSPENDED);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER))
                .isInstanceOf(InvalidStatusException.class);
    }
}