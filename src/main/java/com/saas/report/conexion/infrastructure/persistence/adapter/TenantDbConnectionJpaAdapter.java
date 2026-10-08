package com.saas.report.conexion.infrastructure.persistence.adapter;

import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.conexion.infrastructure.persistence.TenantDbConnectionJpaRepository;
import com.saas.report.conexion.infrastructure.persistence.mapper.TenantDbConnectionMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class TenantDbConnectionJpaAdapter implements TenantDbConnectionRepository {

    private final TenantDbConnectionJpaRepository repository;

    public TenantDbConnectionJpaAdapter(TenantDbConnectionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDbConnection> findById(TenantDbConnectionUUID dbConnectionId) {
        return repository.findById(dbConnectionId.getValue()).map(TenantDbConnectionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDbConnection> findByTenantId(TenantUUID tenantId) {
        return repository.findByTenantId(tenantId.getValue()).map(TenantDbConnectionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDbConnection> findAllByTenantId(TenantUUID tenantId) {
        return repository.findAllByTenantId(tenantId.getValue()).stream()
                .map(TenantDbConnectionMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public TenantDbConnection save(TenantDbConnection connection) {
        return TenantDbConnectionMapper.toDomain(repository.save(TenantDbConnectionMapper.toJpa(connection)));
    }
}