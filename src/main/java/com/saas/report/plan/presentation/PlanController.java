package com.saas.report.plan.presentation;

import com.saas.report.plan.application.dto.PlanRequest;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.application.usecase.ActivatePlanUseCase;
import com.saas.report.plan.application.usecase.CreatePlanUseCase;
import com.saas.report.plan.application.usecase.DeactivatePlanUseCase;
import com.saas.report.plan.application.usecase.GetPlanUseCase;
import com.saas.report.plan.application.usecase.ListPlansUseCase;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Administración del catálogo de planes de la plataforma.
 *
 * <p>Gestiona el alta y la consulta de planes y, sobre todo, su activación y
 * desactivación: un plan inactivo no puede ser contratado ni ser destino de un
 * cambio de plan en una suscripción.
 *
 * <p>Las rutas cuelgan de {@code /admin}. Solo la activación y la desactivación
 * exigen el rol {@code PLATFORM_ADMIN}; el resto admite también
 * {@code PLATFORM_USER}.
 */
@RestController
@RequestMapping("/admin/plans")
public class PlanController {

    private final CreatePlanUseCase createPlanUseCase;
    private final GetPlanUseCase getPlanUseCase;
    private final ListPlansUseCase listPlansUseCase;
    private final ActivatePlanUseCase activatePlanUseCase;
    private final DeactivatePlanUseCase deactivatePlanUseCase;

    public PlanController(CreatePlanUseCase createPlanUseCase,
                          GetPlanUseCase getPlanUseCase,
                          ListPlansUseCase listPlansUseCase,
                          ActivatePlanUseCase activatePlanUseCase,
                          DeactivatePlanUseCase deactivatePlanUseCase) {
        this.createPlanUseCase = createPlanUseCase;
        this.getPlanUseCase = getPlanUseCase;
        this.listPlansUseCase = listPlansUseCase;
        this.activatePlanUseCase = activatePlanUseCase;
        this.deactivatePlanUseCase = deactivatePlanUseCase;
    }

    /**
     * Crea un plan con su lista de precios por tramo de meses.
     *
     * @param request datos del plan y sus precios por tramo de meses
     * @return el plan creado con estado {@code 201 CREATED}
     * @throws PlanNombreAlreadyExistsException si ya existe un plan con ese nombre
     * @throws InvalidAmountException si algún precio es nulo o negativo
     */
    @PostMapping
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        PlanResponse response = createPlanUseCase.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene un plan con sus precios.
     *
     * @param planId identificador del plan
     * @return el plan encontrado
     * @throws ResourceNotFoundException si el plan no existe
     */
    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> get(@PathVariable UUID planId) {
        return ResponseEntity.ok(getPlanUseCase.obtener(new PlanUUID(planId)));
    }

    /**
     * Lista todos los planes del catálogo, sin distinción de estado.
     *
     * @return la lista completa de planes
     */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> list() {
        return ResponseEntity.ok(listPlansUseCase.listar());
    }

    /**
     * Activa un plan, habilitándolo para nuevas suscripciones y cambios de plan.
     *
     * @param planId identificador del plan
     * @return el plan ya activo
     * @throws ResourceNotFoundException si el plan no existe
     * @throws InvalidStatusException si el plan ya estaba activo
     */
    @PostMapping("/{planId}/activar")
    public ResponseEntity<PlanResponse> activate(@PathVariable UUID planId) {
        return ResponseEntity.ok(activatePlanUseCase.activar(new PlanUUID(planId)));
    }

    /**
     * Desactiva un plan, de modo que ya no pueda ser contratado ni ser destino de
     * un cambio de plan.
     *
     * @param planId identificador del plan
     * @return el plan ya desactivado
     * @throws ResourceNotFoundException si el plan no existe
     * @throws InvalidStatusException si el plan ya estaba inactivo
     */
    @PostMapping("/{planId}/desactivar")
    public ResponseEntity<PlanResponse> deactivate(@PathVariable UUID planId) {
        return ResponseEntity.ok(deactivatePlanUseCase.desactivar(new PlanUUID(planId)));
    }
}