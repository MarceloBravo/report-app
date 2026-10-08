package com.saas.report.usuarios.domain.emailverificacion;

import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import java.util.UUID;

public interface EmailVerificationTokenService {

    String generar(UsuarioUUID usuarioId, String email);

    UsuarioUUID verificar(String token);

    Boolean esValido(UUID usuarioId, String token);

    UsuarioUUID extraerUsuarioId(String token);
}