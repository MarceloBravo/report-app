package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetConsultaUseCase {

    private final ConsultaHistorialRepository consultaRepository;

    public GetConsultaUseCase(ConsultaHistorialRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public ConsultaResponse obtener(ConsultaHistorialUUID consultaId) {
        ConsultaHistorial consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consulta no encontrada con ID: " + consultaId.getValue()));

        return ConsultaResponse.from(consulta);
    }
}