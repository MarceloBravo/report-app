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
import com.saas.report.tenant.domain.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterUseCase {

    private final UsuarioAuthRepository usuarioAuthRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCase(UsuarioAuthRepository usuarioAuthRepository,
                           TenantRepository tenantRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrar(RegisterRequest request, UsuarioRol rol, AuthPrincipal creador) {
        if (usuarioAuthRepository.existePorEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
        }

        UsuarioRol rolCreador = rolCreador(creador);
        if (!rolCreador.puedeCrear(rol)) {
            throw new ForbiddenException("El rol " + rolCreador + " no puede crear usuarios con rol " + rol);
        }

        TenantUUID tenantId = resolverTenantId(request, rol, rolCreador, creador);
        validarTenantActivo(tenantId);

        AuthUser usuario = new AuthUser(
                UUID.randomUUID(),
                request.email(),
                request.nombre(),
                rol.name(),
                tenantId != null ? tenantId.getValue() : null);

        usuarioAuthRepository.guardar(usuario, passwordEncoder.encode(request.password()));
    }

    private UsuarioRol rolCreador(AuthPrincipal creador) {
        if (creador == null || creador.rol() == null) {
            throw new ForbiddenException("No autenticado");
        }
        return UsuarioRol.valueOf(creador.rol());
    }

    private TenantUUID resolverTenantId(RegisterRequest request, UsuarioRol rol,
                                        UsuarioRol rolCreador, AuthPrincipal creador) {
        if (rol.esRolPlataforma()) {
            return null;
        }
        if (rolCreador == UsuarioRol.ADMIN) {
            if (creador.tenantId() == null) {
                throw new ForbiddenException("El administrador de tenant no tiene tenant asociado");
            }
            return new TenantUUID(UUID.fromString(creador.tenantId()));
        }
        if (request.tenantId() == null) {
            throw new IllegalArgumentException("Se requiere tenant_id para el rol " + rol);
        }
        return new TenantUUID(request.tenantId());
    }

    private void validarTenantActivo(TenantUUID tenantId) {
        if (tenantId == null) {
            return;
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant no encontrado con ID: " + tenantId.getValue()));

        if (!tenant.estaActivo()) {
            throw new InvalidStatusException("El tenant no se encuentra activo");
        }
    }
}