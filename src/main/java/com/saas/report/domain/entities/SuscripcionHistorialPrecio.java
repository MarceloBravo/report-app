package com.saas.report.domain.entities;

import com.saas.report.shared.exception.InvalidAmountException;
import com.saas.report.domain.valueObjects.SuscripcionHistorialPrecioUUID;
import com.saas.report.plan.domain.PlanPrecio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SuscripcionHistorialPrecio {

    private final SuscripcionHistorialPrecioUUID historialId;
    private final Suscripcion suscripcion;
    private final LocalDate periodoInicio;
    private final LocalDate periodoFin;
    private final BigDecimal precioAplicado;
    private final PlanPrecio planPrecio;

    public SuscripcionHistorialPrecio(SuscripcionHistorialPrecioUUID historialId, Suscripcion suscripcion,
                                      LocalDate periodoInicio, LocalDate periodoFin,
                                      BigDecimal precioAplicado, PlanPrecio planPrecio) {
        if (historialId == null) {
            throw new IllegalArgumentException("El ID del historial de precios no puede ser nulo");
        }
        if (suscripcion == null) {
            throw new IllegalArgumentException("La suscripción no puede ser nula");
        }
        if (periodoInicio == null) {
            throw new IllegalArgumentException("El inicio del período no puede ser nulo");
        }
        if (periodoFin != null && periodoFin.isBefore(periodoInicio)) {
            throw new IllegalArgumentException("El fin del período no puede ser anterior al inicio");
        }
        if (precioAplicado == null || precioAplicado.signum() < 0) {
            throw new InvalidAmountException("El precio aplicado no puede ser nulo ni negativo");
        }
        this.historialId = historialId;
        this.suscripcion = suscripcion;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.precioAplicado = precioAplicado;
        this.planPrecio = planPrecio;
    }

    public SuscripcionHistorialPrecioUUID getHistorialId() {
        return historialId;
    }

    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFin() {
        return periodoFin;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }

    public PlanPrecio getPlanPrecio() {
        return planPrecio;
    }

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(periodoInicio)) {
            return false;
        }
        return periodoFin == null || !hoy.isAfter(periodoFin);
    }

    public Integer getDuracionDias() {
        LocalDate fin = this.periodoFin == null ? LocalDate.now() : this.periodoFin;
        return (int) ChronoUnit.DAYS.between(this.periodoInicio, fin) + 1;
    }

    public BigDecimal getPrecioDiario() {
        int dias = getDuracionDias();
        if (dias <= 0) {
            return BigDecimal.ZERO;
        }
        return this.precioAplicado.divide(BigDecimal.valueOf(dias), 4, RoundingMode.HALF_UP);
    }
}