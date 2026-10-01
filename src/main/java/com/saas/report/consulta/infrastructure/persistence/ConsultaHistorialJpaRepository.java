package com.saas.report.consulta.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConsultaHistorialJpaRepository extends JpaRepository<ConsultaHistorialJpaEntity, UUID> {

    Page<ConsultaHistorialJpaEntity> findAllByTenantIdOrderByFechaConsultaDesc(UUID tenantId, Pageable pageable);

    long countByTenantIdAndFechaConsultaAfter(UUID tenantId, LocalDateTime fechaConsulta);
}