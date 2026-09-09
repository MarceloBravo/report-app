package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario execute(UsuarioUUID usuarioId, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId.getValue()));

        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.cambiarNombre(request.nombre());
        }
        if (request.email() != null && !request.email().isBlank()) {
            Usuario usuarioConEmail = usuarioRepository.findByEmail(request.email()).orElse(null);
            if (usuarioConEmail != null && !usuarioConEmail.getUsuarioId().equals(usuarioId)) {
                throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
            }
            usuario.cambiarEmail(request.email());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.cambiarPassword(passwordEncoder.encode(request.password()));
        }
        if (request.rol() != null) {
            usuario.cambiarRol(request.rol());
        }
        if (request.activo() != null) {
            usuario.cambiarEstado(request.activo());
        }

        return usuarioRepository.save(usuario);
    }
}