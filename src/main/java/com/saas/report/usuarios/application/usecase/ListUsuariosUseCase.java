package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;

    public ListUsuariosUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar(UsuarioRol rol, Boolean activo) {
        if (rol != null) {
            return usuarioRepository.findByRol(rol);
        }
        if (activo != null) {
            return usuarioRepository.findByActivo(activo);
        }
        return usuarioRepository.findAll();
    }
}