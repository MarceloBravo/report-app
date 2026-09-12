package com.saas.report.onboarding.application.dto;

import java.util.UUID;

public record OnboardingResponse(
        UUID tenantId,
        UUID usuarioId,
        UUID suscripcionId,
        UUID dbConnectionId,
        String emailVerificacionUrl
) {
}