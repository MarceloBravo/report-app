package com.saas.report.tenant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {

    @Query(value = """
            SELECT tenantId, nombre_empresa, subdominio_slug, estado, fecha_registro
            FROM tenants
            WHERE CAST(:searchText AS text) IS NULL
                OR LOWER(nombre_empresa) LIKE LOWER(CONCAT('%', :searchText, '%'))
                OR LOWER(subdominio_slug) LIKE LOWER(CONCAT('%', :searchText, '%'))
                OR LOWER(CAST(estado AS text)) LIKE LOWER(CONCAT('%', :searchText, '%'))
            ORDER BY nombre_empresa, tenant_id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM tenants
            WHERE CAST(:searchText AS text) IS NULL
                OR LOWER(nombre_empresa) LIKE LOWER(CONCAT('%', :searchText, '%'))
                OR LOWER(subdominio_slug) LIKE LOWER(CONCAT('%', :searchText, '%'))
                OR LOWER(CAST(estado AS text)) LIKE LOWER(CONCAT('%', :searchText, '%'))
            """,
            nativeQuery = true)
    Page<TenantJpaEntity> findAll(@Param("searchText") String searchText, Pageable pageable);

    boolean existsBySubdominioSlug(String subdominioSlug);

}