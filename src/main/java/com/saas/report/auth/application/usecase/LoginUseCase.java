package com.saas.report.auth.application.usecase;

import com.saas.report.auth.application.AuthTokenIssuer;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final AuthTokenIssuer tokenIssuer;

    public LoginUseCase(AuthenticationManager authenticationManager,
                        UsuarioRepository usuarioRepository,
                        AuthTokenIssuer tokenIssuer) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        return tokenIssuer.emitirTokens(usuario);
    }
}