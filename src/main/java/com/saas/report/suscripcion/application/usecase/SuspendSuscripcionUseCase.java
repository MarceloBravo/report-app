package com.saas.report.suscripcion.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspendSuscripcionUseCase {

    private final SuscripcionRepository suscripcionRepository;

    public SuspendSuscripcionUseCase(SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
    }

    @Transactional
    public SuscripcionResponse suspender(SuscripcionUUID suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Suscripción no encontrada con ID: " + suscripcionId.getValue()));

        suscripcion.suspender();

        return SuscripcionResponse.from(suscripcionRepository.save(suscripcion));
    }
}