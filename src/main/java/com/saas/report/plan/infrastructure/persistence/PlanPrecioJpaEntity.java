package com.saas.report.plan.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "plan_precios")
@Getter
@Setter
@NoArgsConstructor
public class PlanPrecioJpaEntity {

    @Id
    @Column(name = "plan_precio_id", columnDefinition = "uuid")
    private UUID planPrecioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanJpaEntity plan;

    @Column(name = "mes_desde", nullable = false)
    private Integer mesDesde;

    @Column(name = "mes_hasta")
    private Integer mesHasta;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
}