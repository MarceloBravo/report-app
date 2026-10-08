package com.saas.report.usuarios.application.usecase;

import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VerificarEmailUseCase {

    private final UsuarioRepository usuarioRepository;
    private final EmailVerificationTokenService tokenService;

    public VerificarEmailUseCase(UsuarioRepository usuarioRepository, EmailVerificationTokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public void ejecutar(UUID usuarioId, String token) {
        ejecutar(new UsuarioUUID(usuarioId), token);
    }

    @Transactional
    public void ejecutar(UsuarioUUID usuarioId, String token) {
        if (!tokenService.esValido(usuarioId.getValue(), token)) {
            throw new ForbiddenException("Token de verificación inválido o expirado");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.verificarEmail();
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void verificar(String token) {
        if (token == null || token.isBlank()) {
            throw new ForbiddenException("Token inválido");
        }
        UsuarioUUID uid = tokenService.extraerUsuarioId(token);
        ejecutar(uid, token);
    }
}