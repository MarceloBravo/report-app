package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario execute(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
        }

        Usuario usuario = new Usuario(
                UsuarioUUID.generate(),
                new TenantUUID(request.tenantId()),
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.rol() != null ? request.rol() : UsuarioRol.VIEWER,
                request.activo() != null ? request.activo() : true);

        return usuarioRepository.save(usuario);
    }
}