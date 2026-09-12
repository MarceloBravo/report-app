package com.saas.report.usuarios.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificarEmailUseCase {

    private final EmailVerificationTokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public VerificarEmailUseCase(EmailVerificationTokenService tokenService,
                                 UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void verificar(String token) {
        UsuarioUUID usuarioId = tokenService.verificar(token);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!usuario.getEmailVerificado()) {
            usuario.marcarEmailComoVerificado();
            usuarioRepository.save(usuario);
        }
    }
}