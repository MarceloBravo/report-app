package com.saas.report.suscripcion.presentation;

import com.saas.report.suscripcion.application.dto.ActualizarPlanRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.application.usecase.ActualizarPlanUseCase;
import com.saas.report.suscripcion.application.usecase.CancelarSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.CrearSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.SuspenderSuscripcionUseCase;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/suscripciones")
public class SuscripcionController {

    private final CrearSuscripcionUseCase crearSuscripcionUseCase;
    private final CancelarSuscripcionUseCase cancelarSuscripcionUseCase;
    private final SuspenderSuscripcionUseCase suspenderSuscripcionUseCase;
    private final ActualizarPlanUseCase actualizarPlanUseCase;

    public SuscripcionController(CrearSuscripcionUseCase crearSuscripcionUseCase,
                                 CancelarSuscripcionUseCase cancelarSuscripcionUseCase,
                                 SuspenderSuscripcionUseCase suspenderSuscripcionUseCase,
                                 ActualizarPlanUseCase actualizarPlanUseCase) {
        this.crearSuscripcionUseCase = crearSuscripcionUseCase;
        this.cancelarSuscripcionUseCase = cancelarSuscripcionUseCase;
        this.suspenderSuscripcionUseCase = suspenderSuscripcionUseCase;
        this.actualizarPlanUseCase = actualizarPlanUseCase;
    }

    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@Valid @RequestBody SuscripcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crearSuscripcionUseCase.crear(request));
    }

    @PostMapping("/{suscripcionId}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(cancelarSuscripcionUseCase.cancelar(new SuscripcionUUID(suscripcionId)));
    }

    @PostMapping("/{suscripcionId}/suspender")
    public ResponseEntity<SuscripcionResponse> suspender(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(suspenderSuscripcionUseCase.suspender(new SuscripcionUUID(suscripcionId)));
    }

    @PutMapping("/{suscripcionId}/plan")
    public ResponseEntity<SuscripcionResponse> actualizarPlan(@PathVariable UUID suscripcionId,
                                                              @Valid @RequestBody ActualizarPlanRequest request) {
        return ResponseEntity.ok(actualizarPlanUseCase.actualizarPlan(new SuscripcionUUID(suscripcionId), request));
    }
}