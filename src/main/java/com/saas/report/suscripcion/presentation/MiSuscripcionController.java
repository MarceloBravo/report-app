package com.saas.report.suscripcion.presentation;

import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.application.usecase.GetMiSuscripcionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suscripcion")
public class MiSuscripcionController {

    private final GetMiSuscripcionUseCase getMiSuscripcionUseCase;

    public MiSuscripcionController(GetMiSuscripcionUseCase getMiSuscripcionUseCase) {
        this.getMiSuscripcionUseCase = getMiSuscripcionUseCase;
    }

    @GetMapping("/mia")
    public ResponseEntity<MiSuscripcionResponse> mia() {
        return ResponseEntity.ok(getMiSuscripcionUseCase.obtenerMiSuscripcion(SecurityContextAuth.tenantIdActual()));
    }
}