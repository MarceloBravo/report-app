package com.saas.report.usuarios.application.usecase;

import com.saas.report.usuarios.application.dto.UsuarioResponse;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;

    public ListUsuariosUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(UsuarioRol rol, Boolean activo, Integer page, Integer size) {
        Page<Usuario> usuarios = usuarioRepository.findAll(rol, activo, page, size);
        return usuarios.map(UsuarioResponse::from);
    }
}