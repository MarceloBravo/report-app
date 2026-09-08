package com.saas.report.suscripcion.infrastructure.persistence.adapter;

import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.suscripcion.infrastructure.persistence.SuscripcionJpaRepository;
import com.saas.report.suscripcion.infrastructure.persistence.mapper.SuscripcionMapper;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class SuscripcionJpaAdapter implements SuscripcionRepository {

    private final SuscripcionJpaRepository repository;

    public SuscripcionJpaAdapter(SuscripcionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Suscripcion> findById(SuscripcionUUID suscripcionId) {
        return repository.findById(suscripcionId.getValue()).map(SuscripcionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Suscripcion> findByTenantIdAndEstado(TenantUUID tenantId, SuscripcionEstado estado) {
        return repository.findByTenantIdAndEstado(tenantId.getValue(), estado).map(SuscripcionMapper::toDomain);
    }

    @Override
    @Transactional
    public Suscripcion save(Suscripcion suscripcion) {
        return SuscripcionMapper.toDomain(
                repository.save(SuscripcionMapper.toJpa(suscripcion)));
    }
}