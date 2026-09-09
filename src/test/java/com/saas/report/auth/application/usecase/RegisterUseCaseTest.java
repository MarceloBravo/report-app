package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.exception.ForbiddenException;
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
import static org.mockito.Mockito.never;
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

    private AuthPrincipal creador(UsuarioRol rol, String tenantId) {
        return new AuthPrincipal("jefe@saas.com", "u1", tenantId, rol.name());
    }

    @Test
    void registraUsuarioConPasswordEncriptada() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.ADMIN, tenant.getTenantId().getValue().toString()));

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
    void platformAdminRegistraUsuarioConRolAdmin() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.ADMIN, creador(UsuarioRol.PLATFORM_ADMIN, null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.ADMIN);
    }

    @Test
    void rechazaEmailYaRegistrado() {
        RegisterRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void rechazaTenantInexistente() {
        RegisterRequest request = request();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaTenantInactivo() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.SUSPENDED);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void administradorTenantCreaViewerEnSuPropioTenant() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.ADMIN, tenant.getTenantId().getValue().toString()));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.VIEWER);
        assertThat(captor.getValue().getTenantId()).isEqualTo(tenant.getTenantId());
    }

    @Test
    void administradorTenantNoPuedeCrearOtroAdmin() {
        RegisterRequest request = request();

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.ADMIN,
                creador(UsuarioRol.ADMIN, UUID.randomUUID().toString())))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void viewerNoPuedeCrearUsuarios() {
        RegisterRequest request = request();

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.VIEWER, UUID.randomUUID().toString())))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void platformUserPuedeCrearAdminDeTenant() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.ADMIN, creador(UsuarioRol.PLATFORM_USER, null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(captor.getValue().getTenantId().getValue()).isEqualTo(request.tenantId());
    }

    @Test
    void platformUserNoPuedeCrearRolesDePlataforma() {
        RegisterRequest request = request();

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.PLATFORM_ADMIN,
                creador(UsuarioRol.PLATFORM_USER, null)))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.PLATFORM_USER,
                creador(UsuarioRol.PLATFORM_USER, null)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void plataformAdminCreaRolPlataformaSinTenant() {
        RegisterRequest request = new RegisterRequest("Pam", "pam@saas.com", "secreto123", null, null);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.PLATFORM_USER, creador(UsuarioRol.PLATFORM_ADMIN, null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(UsuarioRol.PLATFORM_USER);
        assertThat(captor.getValue().getTenantId()).isNull();
        verify(tenantRepository, never()).findById(any());
    }

    @Test
    void rechazaRolTenantSinTenantIdEnElRequest() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@empresa.com", "secreto123", null, null);

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaCreadorNoAutenticado() {
        RegisterRequest request = request();

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER, null))
                .isInstanceOf(ForbiddenException.class);
    }
}