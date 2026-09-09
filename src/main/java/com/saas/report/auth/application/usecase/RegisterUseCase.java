package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.domain.exception.EmailAlreadyExistsException;
import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.user.UsuarioRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCase(UsuarioRepository usuarioRepository,
                           TenantRepository tenantRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrar(RegisterRequest request, UsuarioRol rol) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
        }

        Tenant tenant = tenantRepository.findById(new TenantUUID(request.tenantId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant no encontrado con ID: " + request.tenantId()));

        if (!tenant.estaActivo()) {
            throw new InvalidStatusException("El tenant no se encuentra activo");
        }

        Usuario usuario = new Usuario(
                UsuarioUUID.generate(),
                tenant.getTenantId(),
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                rol,
                true);

        usuarioRepository.save(usuario);
    }
}