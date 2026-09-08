package com.saas.report.plan.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class PlanJpaEntity {

    @Id
    @Column(name = "plan_id", columnDefinition = "uuid")
    private UUID planId;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 60)
    private String nombrePlan;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "max_usuarios", nullable = false)
    private Integer maxUsuarios;

    @Column(name = "limite_consultas_mensuales", nullable = false)
    private Integer limiteConsultasMensuales;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    private List<PlanPrecioJpaEntity> precios = new ArrayList<>();
}