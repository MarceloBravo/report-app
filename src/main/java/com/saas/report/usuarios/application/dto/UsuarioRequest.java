package com.saas.report.usuarios.application.dto;

import com.saas.report.identity.domain.user.UsuarioRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioRequest(
        @NotNull(message = "El tenant es requerido")
        UUID tenantId,

        @NotBlank(message = "El nombre es requerido")
        String nombre,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        UsuarioRol rol,

        Boolean activo
) {
}