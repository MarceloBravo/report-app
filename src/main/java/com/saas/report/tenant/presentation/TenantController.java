package com.saas.report.tenant.presentation;

import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.application.usecase.ActivateTenantUseCase;
import com.saas.report.tenant.application.usecase.CancelTenantUseCase;
import com.saas.report.tenant.application.usecase.GetTenantUseCase;
import com.saas.report.tenant.application.usecase.SuspendTenantUseCase;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/tenants")
public class TenantController {

    private final GetTenantUseCase getTenantUseCase;
    private final ActivateTenantUseCase activateTenantUseCase;
    private final SuspendTenantUseCase suspendTenantUseCase;
    private final CancelTenantUseCase cancelTenantUseCase;

    public TenantController(GetTenantUseCase getTenantUseCase,
                            ActivateTenantUseCase activateTenantUseCase,
                            SuspendTenantUseCase suspendTenantUseCase,
                            CancelTenantUseCase cancelTenantUseCase) {
        this.getTenantUseCase = getTenantUseCase;
        this.activateTenantUseCase = activateTenantUseCase;
        this.suspendTenantUseCase = suspendTenantUseCase;
        this.cancelTenantUseCase = cancelTenantUseCase;
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> get(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(getTenantUseCase.obtener(new TenantUUID(tenantId)));
    }

    @PostMapping("/{tenantId}/activate")
    public ResponseEntity<TenantResponse> activate(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(activateTenantUseCase.activar(new TenantUUID(tenantId)));
    }

    @PostMapping("/{tenantId}/suspend")
    public ResponseEntity<TenantResponse> suspend(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(suspendTenantUseCase.suspender(new TenantUUID(tenantId)));
    }

    @PostMapping("/{tenantId}/cancel")
    public ResponseEntity<TenantResponse> cancel(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(cancelTenantUseCase.cancelar(new TenantUUID(tenantId)));
    }
}