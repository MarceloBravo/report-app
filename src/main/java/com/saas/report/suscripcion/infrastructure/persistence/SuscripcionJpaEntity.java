package com.saas.report.suscripcion.infrastructure.persistence;

import com.saas.report.suscripcion.domain.SuscripcionEstado;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "suscripciones")
@Getter
@Setter
@NoArgsConstructor
public class SuscripcionJpaEntity {

    @Id
    @Column(name = "suscripcion_id", columnDefinition = "uuid")
    private UUID suscripcionId;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "plan_id", nullable = false, columnDefinition = "uuid")
    private UUID planId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private SuscripcionEstado estado;

    @Column(name = "proxima_facturacion")
    private LocalDate proximaFacturacion;

    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("periodoInicio ASC")
    private List<SuscripcionHistorialPrecioJpaEntity> historialPrecios = new ArrayList<>();
}