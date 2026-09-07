package com.saas.report.plan.domain;

import com.saas.report.shared.exception.InvalidAmountException;
import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;

import java.math.BigDecimal;
import java.util.Objects;

public class PlanPrecio {

    private final PlanPrecioUUID planPrecioId;
    private final PlanUUID planId;
    private final Integer mesDesde;
    private final Integer mesHasta;
    private final BigDecimal precio;

    public PlanPrecio(PlanPrecioUUID planPrecioId, PlanUUID planId, Integer mesDesde, Integer mesHasta, BigDecimal precio) {
        if (planPrecioId == null) {
            throw new IllegalArgumentException("El ID del tramo de precio no puede ser nulo");
        }
        if (planId == null) {
            throw new IllegalArgumentException("El ID del plan no puede ser nulo");
        }
        if (mesDesde == null || mesDesde < 1) {
            throw new InvalidIntegerValue("El mes de inicio del tramo debe ser >= 1");
        }
        if (mesHasta != null && mesHasta < mesDesde) {
            throw new InvalidIntegerValue("El mes de fin del tramo no puede ser anterior al mes de inicio");
        }
        if (precio == null || precio.signum() < 0) {
            throw new InvalidAmountException("El precio del tramo no puede ser nulo ni negativo");
        }
        this.planPrecioId = planPrecioId;
        this.planId = planId;
        this.mesDesde = mesDesde;
        this.mesHasta = mesHasta;
        this.precio = precio;
    }

    public PlanPrecioUUID getPlanPrecioId() {
        return planPrecioId;
    }

    public PlanUUID getPlanId() {
        return planId;
    }

    public Integer getMesDesde() {
        return mesDesde;
    }

    public Integer getMesHasta() {
        return mesHasta;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean aplicaParaMes(Integer mesesContratados) {
        if (mesesContratados == null || mesesContratados < 1) {
            return false;
        }
        if (mesesContratados < mesDesde) {
            return false;
        }
        return mesHasta == null || mesesContratados <= mesHasta;
    }

    public boolean esTramoFinal() {
        return mesHasta == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlanPrecio that = (PlanPrecio) o;
        return Objects.equals(mesDesde, that.mesDesde)
                && Objects.equals(mesHasta, that.mesHasta)
                && Objects.equals(precio, that.precio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mesDesde, mesHasta, precio);
    }
}