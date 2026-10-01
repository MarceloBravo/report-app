package com.saas.report.suscripcion.infrastructure.persistence;

import com.saas.report.suscripcion.domain.SuscripcionEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuscripcionJpaRepository extends JpaRepository<SuscripcionJpaEntity, UUID> {

    Optional<SuscripcionJpaEntity> findByTenantIdAndEstado(UUID tenantId, SuscripcionEstado estado);

    List<SuscripcionJpaEntity> findByTenantIdOrderByFechaInicioDesc(UUID tenantId);
}