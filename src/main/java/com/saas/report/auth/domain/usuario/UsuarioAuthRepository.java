package com.saas.report.auth.domain.usuario;

import java.util.Optional;

public interface UsuarioAuthRepository {

    Optional<AuthCredentials> buscarCredenciales(String email);

    Optional<AuthUser> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    AuthUser guardar(AuthUser usuario, String passwordHash);
}