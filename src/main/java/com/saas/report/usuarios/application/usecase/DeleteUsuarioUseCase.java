package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public DeleteUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void execute(UsuarioUUID usuarioId) {
        if (usuarioRepository.findById(usuarioId).isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId.getValue());
        }
        usuarioRepository.delete(usuarioId);
    }
}