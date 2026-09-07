package com.saas.report.infrastructure.persistence;

import com.saas.report.domain.enums.TenantEstado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class TenantJpaEntity {

    @Id
    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @Column(name = "subdominio_slug", nullable = false, unique = true)
    private String subdominioSlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private TenantEstado estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}