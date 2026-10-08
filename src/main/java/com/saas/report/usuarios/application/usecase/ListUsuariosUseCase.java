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
        //Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> usuarios = usuarioRepository.findAll(rol != null ? rol : null, activo, page, size);
        return usuarios.map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> execute(String rol, Boolean activo, Integer page, Integer size) {
        UsuarioRol filtroRol = rol != null ? UsuarioRol.valueOf(rol) : null;
        Page<Usuario> usuarios = usuarioRepository.findAll(filtroRol != null ? filtroRol : null, activo, page, size);
        return usuarios.map(UsuarioResponse::from);
    }
}