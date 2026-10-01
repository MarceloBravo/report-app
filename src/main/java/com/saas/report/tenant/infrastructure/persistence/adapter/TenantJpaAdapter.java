package com.saas.report.tenant.infrastructure.persistence.adapter;

import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaRepository;
import com.saas.report.tenant.infrastructure.persistence.mapper.TenantMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.saas.report.shared.helpers.PaginationHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Component
public class TenantJpaAdapter implements TenantRepository {

    private final TenantJpaRepository repository;

    public TenantJpaAdapter(TenantJpaRepository repository) {
        this.repository = repository;
    }

    private String normalizar(String searchTextString) {
        return (searchTextString == null || searchTextString.isBlank()) ? null : searchTextString.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Tenant> findAll(String searchTextString, Integer page, Integer size){
        Pageable pageable = PaginationHelper.getPageable(page, size);
        return repository.findAll(normalizar(searchTextString), pageable)
                .map(TenantMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(TenantUUID tenantId) {
        return repository.findById(tenantId.getValue()).map(TenantMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(TenantUUID tenantId) {
        return repository.existsById(tenantId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySubdominioSlug(String subdominioSlug) {
        return repository.existsBySubdominioSlug(subdominioSlug);
    }

    @Override
    @Transactional
    public Tenant save(Tenant tenant) {
        return TenantMapper.toDomain(repository.save(TenantMapper.toJpa(tenant)));
    }
}