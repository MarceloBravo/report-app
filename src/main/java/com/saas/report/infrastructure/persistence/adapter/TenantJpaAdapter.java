package com.saas.report.infrastructure.persistence.adapter;

import com.saas.report.domain.entities.Tenant;
import com.saas.report.domain.repositories.TenantRepository;
import com.saas.report.domain.valueObjects.TenantUUID;
import com.saas.report.infrastructure.persistence.TenantJpaRepository;
import com.saas.report.infrastructure.persistence.mapper.TenantMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class TenantJpaAdapter implements TenantRepository {

    private final TenantJpaRepository repository;

    public TenantJpaAdapter(TenantJpaRepository repository) {
        this.repository = repository;
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
    @Transactional
    public Tenant save(Tenant tenant) {
        return TenantMapper.toDomain(repository.save(TenantMapper.toJpa(tenant)));
    }
}