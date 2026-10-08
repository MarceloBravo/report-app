package com.saas.report.suscripcion.domain;

import com.saas.report.suscripcion.domain.valueobject.SuscripcionHistorialPrecioUUID;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.domain.valueobject.TenantUUID;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Suscripcion {

    private final SuscripcionUUID suscripcionId;
    private final TenantUUID tenantId;
    private PlanUUID planId;
    private final LocalDate fechaInicio;
    private SuscripcionEstado estado;
    private LocalDate proximaFacturacion;
    private final List<SuscripcionHistorialPrecio> historialPrecios = new ArrayList<>();

    public Suscripcion(SuscripcionUUID suscripcionId, TenantUUID tenantId, PlanUUID planId, LocalDate fechaInicio,
                       SuscripcionEstado estado, LocalDate proximaFacturacion) {
        if (suscripcionId == null) {
            throw new IllegalArgumentException("El ID de la suscripción no puede ser nulo");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (planId == null) {
            throw new IllegalArgumentException("El plan no puede ser nulo");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.suscripcionId = suscripcionId;
        this.tenantId = tenantId;
        this.planId = planId;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.proximaFacturacion = proximaFacturacion;
    }

    public SuscripcionUUID getSuscripcionId() {
        return suscripcionId;
    }

    public TenantUUID getTenantId() {
        return tenantId;
    }

    public PlanUUID getPlanId() {
        return planId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public SuscripcionEstado getEstado() {
        return estado;
    }

    public LocalDate getProximaFacturacion() {
        return proximaFacturacion;
    }

    public Integer getMesesContratados() {
        return (int) Period.between(this.fechaInicio, LocalDate.now()).toTotalMonths();
    }

    public boolean estaVigente() {
        return this.estado == SuscripcionEstado.ACTIVE;
    }

    public boolean requiereCambioDeTramo(Plan plan) {
        if (!estaVigente()) {
            return false;
        }
        if (plan == null || !this.planId.equals(plan.getPlanId())) {
            throw new IllegalArgumentException("El plan no corresponde a la suscripción");
        }
        PlanPrecio tramoActual = plan.getPrecioParaMes(getMesesContratados());
        PlanPrecio tramoProximo = plan.getPrecioParaMes(getMesesContratados() + 1);
        return !tramoActual.equals(tramoProximo);
    }

    public void cancelar() {
        if (this.estado == SuscripcionEstado.CANCELED) {
            throw new InvalidStatusException("La suscripción ya está cancelada.");
        }
        this.estado = SuscripcionEstado.CANCELED;
    }

    public void suspender() {
        if (this.estado == SuscripcionEstado.SUSPENDED) {
            throw new InvalidStatusException("La suscripción ya está suspendida.");
        }
        if (this.estado == SuscripcionEstado.CANCELED) {
            throw new InvalidStatusException("Una suscripción cancelada no puede suspenderse.");
        }
        this.estado = SuscripcionEstado.SUSPENDED;
    }

    /**
     * Cambia el plan contratado y registra el cambio en el historial de precios.
     *
     * <p>El período de precio vigente se cierra el día anterior al cambio y se abre
     * uno nuevo desde la fecha del cambio, con el precio del tramo del plan de destino
     * que corresponde a los meses ya contrato.
     *
     * @param nuevoPlan plan al que se mueve la suscripción; debe estar activo
     * @throws IllegalArgumentException si el plan es nulo o si ya es el plan actual
     * @throws InvalidStatusException si el plan de destino está inactivo
     */
    public void actualizarPlan(Plan nuevoPlan) {
        if (nuevoPlan == null) {
            throw new IllegalArgumentException("El nuevo plan no puede ser nulo");
        }
        if (!nuevoPlan.isActivo()) {
            throw new InvalidStatusException("El plan de destino no está activo.");
        }
        if (this.planId.equals(nuevoPlan.getPlanId())) {
            throw new IllegalArgumentException("La suscripción ya está contratada con ese plan.");
        }
        registrarCambioDePrecio(nuevoPlan, LocalDate.now());
        this.planId = nuevoPlan.getPlanId();
    }

    /**
     * Cierra el período de precio vigente y abre uno nuevo con el precio del tramo
     * que corresponde a los meses contrados de la suscripción.
     *
     * @param nuevoPlan plan de destino, ya validado como activo
     * @param fecha     fecha del cambio de plan
     */
    private void registrarCambioDePrecio(Plan nuevoPlan, LocalDate fecha) {
        this.historialPrecios.stream()
                .filter(h -> h.getPeriodoFin() == null)
                .forEach(h -> h.cerrarPeriodo(fecha.minusDays(1)));

        PlanPrecio tramo = nuevoPlan.getPrecioParaMes(Math.max(getMesesContratados(), 1));
        this.historialPrecios.add(new SuscripcionHistorialPrecio(
                SuscripcionHistorialPrecioUUID.generate(),
                this,
                fecha,
                null,
                tramo.getPrecio(),
                tramo.getPlanPrecioId()));
    }

    public List<SuscripcionHistorialPrecio> getHistorialPrecios() {
        return Collections.unmodifiableList(historialPrecios);
    }

    public void addHistorialPrecio(SuscripcionHistorialPrecio historial) {
        if (historial == null) {
            throw new IllegalArgumentException("El historial de precios no puede ser nulo");
        }
        this.historialPrecios.add(historial);
    }
}