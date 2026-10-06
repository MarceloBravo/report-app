package com.saas.report.auth.application.dto;

/**
 * Respuesta de sesión que no expone los tokens: estos viajan únicamente en las
 * cookies {@code HttpOnly} ({@code access_token} y {@code refresh_token}).
 *
 * @param tokenType   tipo de autenticación, siempre {@code Bearer}
 * @param expiresIn   vigencia del access token en milisegundos
 * @param refreshExpiresIn vigencia del refresh token en milisegundos
 */
public record SessionResponse(
        String tokenType,
        long expiresIn,
        long refreshExpiresIn
) {
}
