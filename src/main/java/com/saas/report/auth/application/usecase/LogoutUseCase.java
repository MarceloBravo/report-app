package com.saas.report.auth.application.usecase;

import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final UsuarioTokenRepository usuarioTokenRepository;

    public LogoutUseCase(UsuarioTokenRepository usuarioTokenRepository) {
        this.usuarioTokenRepository = usuarioTokenRepository;
    }

    @Transactional
    public void cerrarSesion(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new InvalidCredentialsException("El token es requerido");
        }
        usuarioTokenRepository.findByAccessToken(accessToken)
                .ifPresent(registro -> {
                    registro.revocar();
                    usuarioTokenRepository.save(registro);
                });
    }
}