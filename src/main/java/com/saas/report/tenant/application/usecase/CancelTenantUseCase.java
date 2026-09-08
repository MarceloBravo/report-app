package com.saas.report.tenant.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelTenantUseCase {

    private final TenantRepository tenantRepository;

    public CancelTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantResponse cancelar(TenantUUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant no encontrado con ID: " + tenantId.getValue()));

        tenant.cancelar();

        return TenantResponse.from(tenantRepository.save(tenant));
    }
}