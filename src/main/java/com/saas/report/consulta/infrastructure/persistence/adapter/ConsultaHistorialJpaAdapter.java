package com.saas.report.consulta.infrastructure.persistence.adapter;

import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.consulta.infrastructure.persistence.ConsultaHistorialJpaRepository;
import com.saas.report.consulta.infrastructure.persistence.mapper.ConsultaHistorialMapper;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<ConsultaHistorial> findByTenantId(TenantUUID tenantId) {
        return repository.findAllByTenantIdOrderByFechaConsultaDesc(tenantId.getValue()).stream()
                .map(ConsultaHistorialMapper::toDomain)
                .toList();
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