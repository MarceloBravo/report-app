package com.saas.report.auth.application;

import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AuthTokenIssuer {

    private final TokenService tokenService;
    private final UsuarioTokenRepository usuarioTokenRepository;

    public AuthTokenIssuer(TokenService tokenService, UsuarioTokenRepository usuarioTokenRepository) {
        this.tokenService = tokenService;
        this.usuarioTokenRepository = usuarioTokenRepository;
    }

    @Transactional
    public TokenResponse emitirTokens(Usuario usuario) {
        String accessToken = tokenService.generateAccessToken(usuario);
        String refreshToken = tokenService.generateRefreshToken(usuario);

        LocalDateTime fechaCreacion = tokenService.extraerFechaCreacion(accessToken);
        LocalDateTime fechaExpiracionAccess = tokenService.extraerExpiracion(accessToken);
        LocalDateTime fechaExpiracionRefresh = tokenService.extraerExpiracion(refreshToken);

        UsuarioToken registro = new UsuarioToken(
                UsuarioTokenUUID.generate(),
                usuario,
                accessToken,
                refreshToken,
                fechaCreacion,
                fechaExpiracionAccess,
                fechaExpiracionRefresh,
                true);
        usuarioTokenRepository.save(registro);

        long expiresInAccess = Duration.between(fechaCreacion, fechaExpiracionAccess).toMillis();
        long expiresInRefresh = Duration.between(fechaCreacion, fechaExpiracionRefresh).toMillis();

        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInAccess, expiresInRefresh);
    }
}