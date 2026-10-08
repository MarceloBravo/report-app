package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.usuario.UsuarioAuthRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.exception.EmailAlreadyExistsException;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UsuarioAuthRepository usuarioAuthRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUseCase(usuarioAuthRepository, tenantRepository, passwordEncoder);
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

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.ADMIN, tenant.getTenantId().getValue().toString()));

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(usuarioAuthRepository).guardar(captor.capture(), eq("encoded-hash"));
        AuthUser guardado = captor.getValue();
        assertThat(guardado.nombre()).isEqualTo("Ana");
        assertThat(guardado.email()).isEqualTo("ana@empresa.com");
        assertThat(guardado.rol()).isEqualTo("VIEWER");
        assertThat(guardado.tenantId()).isEqualTo(tenant.getTenantId().getValue());
    }

    @Test
    void platformAdminRegistraUsuarioConRolAdmin() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.ADMIN, creador(UsuarioRol.PLATFORM_ADMIN, null));

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(usuarioAuthRepository).guardar(captor.capture(), eq("encoded-hash"));
        assertThat(captor.getValue().rol()).isEqualTo("ADMIN");
    }

    @Test
    void rechazaEmailYaRegistrado() {
        RegisterRequest request = request();

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void rechazaTenantInexistente() {
        RegisterRequest request = request();

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaTenantInactivo() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.SUSPENDED);

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.PLATFORM_ADMIN, null)))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void administradorTenantCreaViewerEnSuPropioTenant() {
        RegisterRequest request = request();
        Tenant tenant = tenant(TenantEstado.ACTIVE);

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.VIEWER,
                creador(UsuarioRol.ADMIN, tenant.getTenantId().getValue().toString()));

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(usuarioAuthRepository).guardar(captor.capture(), eq("encoded-hash"));
        assertThat(captor.getValue().rol()).isEqualTo("VIEWER");
        assertThat(captor.getValue().tenantId()).isEqualTo(tenant.getTenantId().getValue());
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

        when(usuarioAuthRepository.existePorEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(any(TenantUUID.class))).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");

        registerUseCase.registrar(request, UsuarioRol.ADMIN, creador(UsuarioRol.PLATFORM_USER, null));

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(usuarioAuthRepository).guardar(captor.capture(), eq("encoded-hash"));
        assertThat(captor.getValue().rol()).isEqualTo("ADMIN");
        assertThat(captor.getValue().tenantId()).isEqualTo(request.tenantId());
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
}