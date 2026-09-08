package com.saas.report.tenant.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantRequest(

        @NotBlank(message = "El nombre de la empresa es requerido")
        @Size(max = 120, message = "El nombre de la empresa no puede superar los 120 caracteres")
        String nombreEmpresa,

        @NotBlank(message = "El subdominio es requerido")
        @Size(max = 63, message = "El subdominio no puede superar los 63 caracteres")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "El subdominio solo admite minúsculas, números y guiones")
        String subdominioSlug
) {
}