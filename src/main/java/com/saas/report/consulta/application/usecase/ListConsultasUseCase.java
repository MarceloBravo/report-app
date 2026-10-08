package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListConsultasUseCase {

    private final ConsultaHistorialRepository consultaRepository;

    public ListConsultasUseCase(ConsultaHistorialRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponse> listar(TenantUUID tenantId, Integer page, Integer size) {
        Page<ConsultaHistorial> consultas = consultaRepository.findByTenantId(tenantId, page, size);
        return consultas.map(ConsultaResponse::from);
    }
}