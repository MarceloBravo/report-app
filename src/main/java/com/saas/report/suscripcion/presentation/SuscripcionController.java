package com.saas.report.suscripcion.presentation;

import com.saas.report.suscripcion.application.dto.ActualizarPlanRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.application.usecase.CancelSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.CreateSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.SuspendSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.UpdateSuscripcionPlanUseCase;
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

    private final CreateSuscripcionUseCase createSuscripcionUseCase;
    private final CancelSuscripcionUseCase cancelSuscripcionUseCase;
    private final SuspendSuscripcionUseCase suspendSuscripcionUseCase;
    private final UpdateSuscripcionPlanUseCase updateSuscripcionPlanUseCase;

    public SuscripcionController(CreateSuscripcionUseCase createSuscripcionUseCase,
                                 CancelSuscripcionUseCase cancelSuscripcionUseCase,
                                 SuspendSuscripcionUseCase suspendSuscripcionUseCase,
                                 UpdateSuscripcionPlanUseCase updateSuscripcionPlanUseCase) {
        this.createSuscripcionUseCase = createSuscripcionUseCase;
        this.cancelSuscripcionUseCase = cancelSuscripcionUseCase;
        this.suspendSuscripcionUseCase = suspendSuscripcionUseCase;
        this.updateSuscripcionPlanUseCase = updateSuscripcionPlanUseCase;
    }

    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@Valid @RequestBody SuscripcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuscripcionUseCase.crear(request));
    }

    @PostMapping("/{suscripcionId}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(cancelSuscripcionUseCase.cancelar(new SuscripcionUUID(suscripcionId)));
    }

    @PostMapping("/{suscripcionId}/suspender")
    public ResponseEntity<SuscripcionResponse> suspender(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(suspendSuscripcionUseCase.suspender(new SuscripcionUUID(suscripcionId)));
    }

    @PutMapping("/{suscripcionId}/plan")
    public ResponseEntity<SuscripcionResponse> actualizarPlan(@PathVariable UUID suscripcionId,
                                                              @Valid @RequestBody ActualizarPlanRequest request) {
        return ResponseEntity.ok(updateSuscripcionPlanUseCase.actualizarPlan(new SuscripcionUUID(suscripcionId), request));
    }
}