package com.saas.report.auth.domain.user;

import com.saas.report.auth.domain.valueobject.UsuarioUUID;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(UsuarioUUID usuarioId);

    boolean existsByEmail(String email);

    Usuario save(Usuario usuario);
}