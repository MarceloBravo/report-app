package com.saas.report.auth.domain.token;

import java.util.Optional;

public interface UsuarioTokenRepository {

    Optional<UsuarioToken> findByAccessToken(String accessToken);

    Optional<UsuarioToken> findByRefreshToken(String refreshToken);

    UsuarioToken save(UsuarioToken usuarioToken);
}