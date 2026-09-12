package com.saas.report.usuarios.domain.emailverificacion;

import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;

public interface EmailVerificationTokenService {

    String generar(UsuarioUUID usuarioId, String email);

    UsuarioUUID verificar(String token);
}