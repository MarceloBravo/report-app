package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultarHistorialUseCase {

    private final ConsultaHistorialRepository consultaRepository;

    public ConsultarHistorialUseCase(ConsultaHistorialRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar(TenantUUID tenantId) {
        return consultaRepository.findByTenantId(tenantId).stream()
                .map(ConsultaResponse::from)
                .toList();
    }
}