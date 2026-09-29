package com.saas.report.tenant.domain;

import com.saas.report.tenant.domain.valueobject.TenantUUID;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TenantRepository {

    Page<Tenant> findAll(String searchTextString, Integer page, Integer size);

    Optional<Tenant> findById(TenantUUID tenantId);

    boolean existsById(TenantUUID tenantId);

    boolean existsBySubdominioSlug(String subdominioSlug);

    Tenant save(Tenant tenant);
}