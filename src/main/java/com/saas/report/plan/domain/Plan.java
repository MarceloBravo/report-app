package com.saas.report.plan.domain;

import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Plan {

    private final PlanUUID planId;
    private final String nombrePlan;
    private final String descripcion;
    private final PlanMaxUsuarios maxUsuarios;
    private final PlanLimiteConsultasMensuales limiteConsultasMensuales;
    private final Boolean activo;
    private final List<PlanPrecio> precios;

    public Plan(PlanUUID planId, String nombrePlan, String descripcion, PlanMaxUsuarios maxUsuarios,
                PlanLimiteConsultasMensuales limiteConsultasMensuales, Boolean activo, List<PlanPrecio> precios) {
        if (planId == null) {
            throw new IllegalArgumentException("El ID del plan no puede ser nulo");
        }
        if (nombrePlan == null || nombrePlan.isBlank()) {
            throw new IllegalArgumentException("El nombre del plan no puede estar vacío");
        }
        if (descripcion == null) {
            throw new IllegalArgumentException("La descripción del plan no puede ser nula");
        }
        if (maxUsuarios == null) {
            throw new IllegalArgumentException("El máximo de usuarios no puede ser nulo");
        }
        if (limiteConsultasMensuales == null) {
            throw new IllegalArgumentException("El límite de consultas mensuales no puede ser nulo (usar -1 para ilimitado)");
        }
        if (activo == null) {
            throw new IllegalArgumentException("El estado de activación no puede ser nulo");
        }
        if (precios == null || precios.isEmpty()) {
            throw new IllegalArgumentException("El plan debe definir al menos un tramo de precio");
        }
        if (precios.stream().anyMatch(precio -> !planId.equals(precio.getPlanId()))) {
            throw new IllegalArgumentException("Todos los tramos de precio deben pertenecer al mismo plan");
        }
        this.planId = planId;
        this.nombrePlan = nombrePlan;
        this.descripcion = descripcion;
        this.maxUsuarios = maxUsuarios;
        this.limiteConsultasMensuales = limiteConsultasMensuales;
        this.activo = activo;
        this.precios = new ArrayList<>(precios);
    }

    public PlanUUID getPlanId() {
        return planId;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public PlanMaxUsuarios getMaxUsuarios() {
        return maxUsuarios;
    }

    public PlanLimiteConsultasMensuales getLimiteConsultasMensuales() {
        return limiteConsultasMensuales;
    }

    public Boolean isActivo() {
        return activo;
    }

    public boolean esIlimitado() {
        return this.limiteConsultasMensuales.esIlimitado();
    }

    public boolean permiteUsuarios(Integer cantidad) {
        if (cantidad == null || cantidad < 0) {
            return false;
        }
        return cantidad <= this.maxUsuarios.getValue();
    }

    public boolean permiteConsultas(Integer cantidad) {
        if (esIlimitado()) {
            return true;
        }
        if (cantidad == null || cantidad < 0) {
            return false;
        }
        return cantidad <= this.limiteConsultasMensuales.getValue();
    }

    public PlanPrecio getPrecioParaMes(Integer mesesContratados) {
        if (mesesContratados == null) {
            throw new IllegalArgumentException("Los meses contratados no pueden ser nulos");
        }
        int mes = Math.max(mesesContratados, 1);
        return this.precios.stream()
                .filter(precio -> precio.aplicaParaMes(mes))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El plan no define tramo de precio para el mes " + mes));
    }

    public List<PlanPrecio> getPrecios() {
        return Collections.unmodifiableList(precios);
    }
}