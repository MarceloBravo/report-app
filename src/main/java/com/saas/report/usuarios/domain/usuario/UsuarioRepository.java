package com.saas.report.usuarios.domain.usuario;

import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(UsuarioUUID usuarioId);

    boolean existsByEmail(String email);

    List<Usuario> findAll();

    List<Usuario> findByRol(UsuarioRol rol);

    List<Usuario> findByActivo(boolean activo);

    Usuario save(Usuario usuario);

    void delete(UsuarioUUID usuarioId);
}