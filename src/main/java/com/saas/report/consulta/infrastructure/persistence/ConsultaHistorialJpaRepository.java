package com.saas.report.consulta.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultaHistorialJpaRepository extends JpaRepository<ConsultaHistorialJpaEntity, UUID> {

    List<ConsultaHistorialJpaEntity> findAllByTenantIdOrderByFechaConsultaDesc(UUID tenantId);

    long countByTenantIdAndFechaConsultaAfter(UUID tenantId, LocalDateTime fechaConsulta);
}