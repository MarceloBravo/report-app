package com.saas.report.domain.entities;

import com.saas.report.domain.enums.SuscripcionEstado;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.domain.valueObjects.SuscripcionUUID;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Suscripcion {

    private final SuscripcionUUID suscripcionId;
    private final TenantUUID tenantId;
    private Plan plan;
    private final LocalDate fechaInicio;
    private SuscripcionEstado estado;
    private LocalDate proximaFacturacion;
    private final List<SuscripcionHistorialPrecio> historialPrecios = new ArrayList<>();

    public Suscripcion(SuscripcionUUID suscripcionId, TenantUUID tenantId, Plan plan, LocalDate fechaInicio,
                       SuscripcionEstado estado, LocalDate proximaFacturacion) {
        if (suscripcionId == null) {
            throw new IllegalArgumentException("El ID de la suscripción no puede ser nulo");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (plan == null) {
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
        this.plan = plan;
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

    public Plan getPlan() {
        return plan;
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

    public boolean requiereCambioDeTramo() {
        if (!estaVigente()) {
            return false;
        }
        PlanPrecio tramoActual = this.plan.getPrecioParaMes(getMesesContratados());
        PlanPrecio tramoProximo = this.plan.getPrecioParaMes(getMesesContratados() + 1);
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

    public void actualizarPlan(Plan nuevoPlan) {
        if (nuevoPlan == null) {
            throw new IllegalArgumentException("El nuevo plan no puede ser nulo");
        }
        if (nuevoPlan.isActivo() == null || !nuevoPlan.isActivo()) {
            throw new InvalidStatusException("El plan de destino no está activo.");
        }
        this.plan = nuevoPlan;
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