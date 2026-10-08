package com.saas.report.consulta.infrastructure.persistence.adapter;

import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.consulta.infrastructure.persistence.ConsultaHistorialJpaRepository;
import com.saas.report.consulta.infrastructure.persistence.mapper.ConsultaHistorialMapper;
import com.saas.report.shared.domain.valueobject.TenantUUID;

import com.saas.report.shared.helpers.PaginationHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ConsultaHistorialJpaAdapter implements ConsultaHistorialRepository {

    private final ConsultaHistorialJpaRepository repository;

    public ConsultaHistorialJpaAdapter(ConsultaHistorialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaHistorial> findById(ConsultaHistorialUUID consultaId) {
        return repository.findById(consultaId.getValue()).map(ConsultaHistorialMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultaHistorial> findByTenantId(TenantUUID tenantId, Integer page, Integer size) {
        Pageable pageable = PaginationHelper.getPageable(page, size);
        return repository.findAllByTenantIdOrderByFechaConsultaDesc(tenantId.getValue(), pageable)
                .map(ConsultaHistorialMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTenantIdAndFechaConsultaAfter(TenantUUID tenantId, LocalDateTime desde) {
        return repository.countByTenantIdAndFechaConsultaAfter(tenantId.getValue(), desde);
    }

    @Override
    @Transactional
    public ConsultaHistorial save(ConsultaHistorial consulta) {
        return ConsultaHistorialMapper.toDomain(
                repository.save(ConsultaHistorialMapper.toJpa(consulta)));
    }
}