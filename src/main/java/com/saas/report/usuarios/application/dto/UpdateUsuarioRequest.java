package com.saas.report.usuarios.application.dto;

import com.saas.report.identity.domain.user.UsuarioRol;
import jakarta.validation.constraints.Email;

public record UpdateUsuarioRequest(
        String nombre,

        @Email(message = "El email no es válido")
        String email,

        String password,

        UsuarioRol rol,

        Boolean activo
) {
}