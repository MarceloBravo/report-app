package com.saas.report.suscripcion.application.dto;

import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionHistorialPrecio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SuscripcionResponse(
        UUID suscripcionId,
        UUID tenantId,
        UUID planId,
        LocalDate fechaInicio,
        SuscripcionEstado estado,
        LocalDate proximaFacturacion,
        Integer mesesContratados,
        Boolean vigente,
        List<SuscripcionHistorialPrecioResponse> historialPrecios
) {

    public static SuscripcionResponse from(Suscripcion suscripcion) {
        return new SuscripcionResponse(
                suscripcion.getSuscripcionId().getValue(),
                suscripcion.getTenantId().getValue(),
                suscripcion.getPlanId().getValue(),
                suscripcion.getFechaInicio(),
                suscripcion.getEstado(),
                suscripcion.getProximaFacturacion(),
                suscripcion.getMesesContratados(),
                suscripcion.estaVigente(),
                suscripcion.getHistorialPrecios().stream()
                        .map(SuscripcionHistorialPrecioResponse::from)
                        .toList());
    }

    public record SuscripcionHistorialPrecioResponse(
            UUID historialId,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            BigDecimal precioAplicado,
            UUID planPrecioId,
            Boolean vigente
    ) {

        public static SuscripcionHistorialPrecioResponse from(SuscripcionHistorialPrecio historial) {
            return new SuscripcionHistorialPrecioResponse(
                    historial.getHistorialId().getValue(),
                    historial.getPeriodoInicio(),
                    historial.getPeriodoFin(),
                    historial.getPrecioAplicado(),
                    historial.getPlanPrecioId().getValue(),
                    historial.estaVigente());
        }
    }
}