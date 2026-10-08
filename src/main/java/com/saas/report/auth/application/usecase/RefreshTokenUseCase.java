package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.exception.TokenRevokedException;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.usuario.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private final TokenService tokenService;
    private final UsuarioTokenRepository usuarioTokenRepository;
    private final AuthTokenIssuer tokenIssuer;

    public RefreshTokenUseCase(TokenService tokenService,
                               UsuarioTokenRepository usuarioTokenRepository,
                               AuthTokenIssuer tokenIssuer) {
        this.tokenService = tokenService;
        this.usuarioTokenRepository = usuarioTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public TokenResponse refrescar(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            throw new InvalidCredentialsException("El refresh token es requerido");
        }
        if (!tokenService.esRefreshToken(refreshTokenString)) {
            throw new TokenRevokedException("El refresh token no es válido");
        }

        UsuarioToken registro = usuarioTokenRepository.findByRefreshToken(refreshTokenString)
                .orElseThrow(() -> new TokenRevokedException("El refresh token ha sido revocado"));

        if (!registro.estaActivo()) {
            throw new TokenRevokedException("El refresh token ha sido revocado");
        }
        if (registro.estaExpiradoRefreshToken()) {
            registro.revocar();
            usuarioTokenRepository.save(registro);
            throw new TokenRevokedException("El refresh token ha expirado");
        }

        AuthUser usuario = registro.getUsuario();
        registro.revocar();
        usuarioTokenRepository.save(registro);

        return tokenIssuer.emitirTokens(usuario);
    }
}