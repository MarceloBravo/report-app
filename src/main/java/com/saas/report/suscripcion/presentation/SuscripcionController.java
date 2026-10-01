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

/**
 * Administración de suscripciones de los tenants de la plataforma.
 *
 * <p>Expone el ciclo de vida completo de una suscripción: alta, cambio de plan,
 * suspensión y cancelación. Todas las rutas cuelgan de {@code /admin} y quedan
 * restringidas a los roles de plataforma {@code PLATFORM_ADMIN} y
 * {@code PLATFORM_USER} (ver {@code SecurityConfig}).
 *
 * <p>Los identificadores viajan como {@link UUID} crudo en la URL y se envuelven
 * en los value objects del dominio antes de invocar los casos de uso.
 */
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

    /**
     * Crea una suscripción para un tenant.
     *
     * @param request datos de la suscripción (tenant, plan, meses contratados)
     * @return la suscripción creada con estado {@code 201 CREATED}
     * @throws ResourceNotFoundException si el tenant o el plan no existen
     */
    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@Valid @RequestBody SuscripcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuscripcionUseCase.crear(request));
    }

    /**
     * Cancela la suscripción, dejándola en estado {@code CANCELED}.
     *
     * @param suscripcionId identificador de la suscripción a cancelar
     * @return la suscripción con su estado ya actualizado
     * @throws ResourceNotFoundException si la suscripción no existe
     * @throws InvalidStatusException si la suscripción no admite la cancelación
     */
    @PostMapping("/{suscripcionId}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(cancelSuscripcionUseCase.cancelar(new SuscripcionUUID(suscripcionId)));
    }

    /**
     * Suspende la suscripción, dejándola en estado {@code SUSPENDED}.
     *
     * @param suscripcionId identificador de la suscripción a suspender
     * @return la suscripción con su estado ya actualizado
     * @throws ResourceNotFoundException si la suscripción no existe
     * @throws InvalidStatusException si la suscripción no admite la suspensión
     */
    @PostMapping("/{suscripcionId}/suspender")
    public ResponseEntity<SuscripcionResponse> suspender(@PathVariable UUID suscripcionId) {
        return ResponseEntity.ok(suspendSuscripcionUseCase.suspender(new SuscripcionUUID(suscripcionId)));
    }

    /**
     * Cambia el plan contratado de una suscripción existente.
     *
     * <p>El {@code suscripcionId} de la URL identifica la suscripción a modificar y
     * el {@code planId} del cuerpo indica el plan de destino. El plan destino debe
     * estar activo; si no, el dominio lanza {@code InvalidStatusException}.
     *
     * @param suscripcionId identificador de la suscripción a actualizar
     * @param request plan de destino ({@code planId})
     * @return la suscripción con el nuevo plan asignado
     * @throws ResourceNotFoundException si la suscripción o el plan no existen
     * @throws InvalidStatusException si el plan de destino no está activo
     */
    @PutMapping("/{suscripcionId}/plan")
    public ResponseEntity<SuscripcionResponse> actualizarPlan(@PathVariable UUID suscripcionId,
                                                              @Valid @RequestBody ActualizarPlanRequest request) {
        return ResponseEntity.ok(updateSuscripcionPlanUseCase.actualizarPlan(new SuscripcionUUID(suscripcionId), request));
    }
}