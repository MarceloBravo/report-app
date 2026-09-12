package com.saas.report.tenant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantDbConnectionJpaRepository extends JpaRepository<TenantDbConnectionJpaEntity, UUID> {

    Optional<TenantDbConnectionJpaEntity> findByTenantId(UUID tenantId);
}