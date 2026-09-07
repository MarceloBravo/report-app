package com.saas.report.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El refresh token es requerido")
        String refreshToken
) {
}