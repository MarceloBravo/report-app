package com.saas.report.usuarios.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.usuarios.application.dto.CreadorPor;
import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.shared.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
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
    public Usuario execute(UsuarioRequest request, CreadorPor creador) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
        }

        UsuarioRol rolCreador = rolCreador(creador);
        UsuarioRol rolNuevo = request.rol() != null ? request.rol() : UsuarioRol.VIEWER;
        if (!rolCreador.puedeCrear(rolNuevo)) {
            throw new ForbiddenException("El rol " + rolCreador + " no puede crear usuarios con rol " + rolNuevo);
        }

        TenantUUID tenantId = resolverTenantId(request, rolNuevo);

        Usuario usuario = new Usuario(
                UsuarioUUID.generate(),
                tenantId,
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                rolNuevo,
                request.activo() != null ? request.activo() : true);

        return usuarioRepository.save(usuario);
    }

    private UsuarioRol rolCreador(CreadorPor creador) {
        if (creador == null || creador.rol() == null) {
            throw new ForbiddenException("No autenticado");
        }
        return UsuarioRol.valueOf(creador.rol());
    }

    private TenantUUID resolverTenantId(UsuarioRequest request, UsuarioRol rol) {
        if (!rol.esRolTenant()) {
            return null;
        }
        if (request.tenantId() == null) {
            throw new IllegalArgumentException("Se requiere tenant_id para el rol " + rol);
        }
        return new TenantUUID(request.tenantId());
    }
}