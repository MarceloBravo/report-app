package com.saas.report.tenant.application.usecase;

import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTenantUseCase {

    private final TenantRepository tenantRepository;

    public GetTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public TenantResponse obtener(TenantUUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant no encontrado con ID: " + tenantId.getValue()));

        return TenantResponse.from(tenant);
    }
}