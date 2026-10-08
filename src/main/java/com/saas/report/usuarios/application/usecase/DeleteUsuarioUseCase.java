package com.saas.report.usuarios.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.usuarios.application.dto.CreadorPor;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public DeleteUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void execute(UUID id) {
        execute(new UsuarioUUID(id));
    }

    @Transactional
    public void execute(UsuarioUUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id.getValue()));
        usuarioRepository.delete(id);
    }

    @Transactional
    public void execute(UUID id, CreadorPor creador) {
        execute(new UsuarioUUID(id), creador);
    }

    @Transactional
    public void execute(UsuarioUUID id, CreadorPor creador) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id.getValue()));
        if (creador != null && creador.rol() != null) {
            UsuarioRol rolCreador = UsuarioRol.valueOf(creador.rol());
            if (!rolCreador.puedeModificar(usuario.getRol())) {
                throw new ForbiddenException("No autorizado para eliminar este usuario");
            }
        }
        usuarioRepository.delete(id);
    }
}