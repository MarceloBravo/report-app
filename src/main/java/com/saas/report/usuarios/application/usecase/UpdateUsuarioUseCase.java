package com.saas.report.usuarios.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.usuarios.application.dto.CreadorPor;
import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario execute(UUID id, UpdateUsuarioRequest request, CreadorPor creador) {
        return execute(new UsuarioUUID(id), request, creador);
    }

    @Transactional
    public Usuario execute(UsuarioUUID id, UpdateUsuarioRequest request, CreadorPor creador) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id.getValue()));

        UsuarioRol rolCreador = creador != null && creador.rol() != null ? UsuarioRol.valueOf(creador.rol()) : null;
        if (rolCreador != null && !rolCreador.puedeModificar(usuario.getRol())) {
            throw new ForbiddenException("No autorizado para modificar este usuario");
        }

        if (request.nombre() != null) {
            usuario.actualizarNombre(request.nombre());
        }
        if (request.email() != null && !request.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new ForbiddenException("El email ya está en uso");
            }
            usuario.actualizarEmail(request.email());
        }
        if (request.password() != null) {
            usuario.actualizarPassword(passwordEncoder.encode(request.password()));
        }
        if (request.rol() != null) {
            if (rolCreador == null || !rolCreador.puedeCrear(request.rol())) {
                throw new ForbiddenException("No autorizado para asignar este rol");
            }
            usuario.cambiarRol(request.rol(), usuario.getTenantId());
        }
        if (request.activo() != null) {
            usuario.actualizarEstado(request.activo());
        }
        if (request.tenantId() != null) {   
            usuario.cambiarTenant(new TenantUUID(request.tenantId()));
        }

        return usuarioRepository.save(usuario);
    }
}