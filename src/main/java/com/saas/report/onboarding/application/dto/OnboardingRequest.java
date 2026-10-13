package com.saas.report.onboarding.application.dto;

import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(

        @Valid
        @NotNull(message = "Los datos del cliente son requeridos")
        ClienteRequest cliente,

        @Valid
        @NotNull(message = "Los datos del usuario son requeridos")
        UsuarioNuevoRequest usuario,

        @Valid
        @NotNull(message = "Los datos de conexión son requeridos")
        ConexionRequest conexion
) {

    public record ClienteRequest(
            @NotBlank(message = "El nombre de la empresa es requerido")
            String nombreEmpresa,

            @NotBlank(message = "El subdominio es requerido")
            @Pattern(regexp = "^[a-z0-9-]+$", message = "El subdominio debe ser un slug válido (minúsculas, números y guiones)")
            String subdominioSlug
    ) {
    }

    public record UsuarioNuevoRequest(
            @NotBlank(message = "El nombre es requerido")
            String nombre,

            @NotBlank(message = "El email es requerido")
            @Email(message = "El email no es válido")
            String email,

            @NotBlank(message = "La contraseña es requerida")
            @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
            String password
    ) {
    }

    public record ConexionRequest(
            @NotBlank(message = "El host es requerido")
            String host,

            @NotNull(message = "El puerto es requerido")
            @Min(value = 1, message = "El puerto debe estar entre 1 y 65535")
            @Max(value = 65535, message = "El puerto debe estar entre 1 y 65535")
            Integer puerto,

            @NotBlank(message = "El nombre de la base es requerido")
            String dbName,

            @NotBlank(message = "El usuario de la base es requerido")
            String dbUser,

            @NotBlank(message = "La contraseña de la base es requerida")
            String dbPassword,

            String schemaName,

            @NotNull(message = "El motor de base de datos es requerido")
            MotorBaseDatos motorBd
    ) {
    }
}