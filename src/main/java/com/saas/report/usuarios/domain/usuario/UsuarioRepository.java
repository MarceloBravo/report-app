package com.saas.report.usuarios.domain.usuario;

import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(UsuarioUUID usuarioId);

    boolean existsByEmail(String email);

    Page<Usuario> findAll(UsuarioRol rol, Boolean activo, Integer page, Integer size);

    Usuario save(Usuario usuario);

    void delete(UsuarioUUID usuarioId);
}