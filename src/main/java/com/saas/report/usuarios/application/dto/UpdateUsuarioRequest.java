package com.saas.report.usuarios.application.dto;

import com.saas.report.identity.domain.user.UsuarioRol;
import jakarta.validation.constraints.Email;
import java.util.UUID;

public record UpdateUsuarioRequest(
        String nombre,

        UUID tenantId,

        @Email(message = "El email no es válido")
        String email,

        String password,

        UsuarioRol rol,

        Boolean activo
) {
}