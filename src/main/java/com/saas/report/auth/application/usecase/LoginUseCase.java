package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.usuario.UsuarioAuthRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final AuthTokenIssuer tokenIssuer;

    public LoginUseCase(AuthenticationManager authenticationManager,
                        UsuarioAuthRepository usuarioAuthRepository,
                        AuthTokenIssuer tokenIssuer) {
        this.authenticationManager = authenticationManager;
        this.usuarioAuthRepository = usuarioAuthRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AuthUser usuario = usuarioAuthRepository.buscarPorEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        return tokenIssuer.emitirTokens(usuario);
    }
}