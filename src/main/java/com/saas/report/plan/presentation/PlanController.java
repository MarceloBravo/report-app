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

    @PostMapping
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        PlanResponse response = createPlanUseCase.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> get(@PathVariable UUID planId) {
        return ResponseEntity.ok(getPlanUseCase.obtener(new PlanUUID(planId)));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> list() {
        return ResponseEntity.ok(listPlansUseCase.listar());
    }

    @PostMapping("/{planId}/activar")
    public ResponseEntity<PlanResponse> activate(@PathVariable UUID planId) {
        return ResponseEntity.ok(activatePlanUseCase.activar(new PlanUUID(planId)));
    }

    @PostMapping("/{planId}/desactivar")
    public ResponseEntity<PlanResponse> deactivate(@PathVariable UUID planId) {
        return ResponseEntity.ok(deactivatePlanUseCase.desactivar(new PlanUUID(planId)));
    }
}