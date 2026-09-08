package com.saas.report.tenant.application.usecase;

import com.saas.report.tenant.application.dto.TenantRequest;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTenantUseCase {

    private final TenantRepository tenantRepository;

    public CreateTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantResponse crear(TenantRequest request) {
        if (tenantRepository.existsBySubdominioSlug(request.subdominioSlug())) {
            throw new TenantSlugAlreadyExistsException(
                    "Ya existe un tenant con el subdominio '" + request.subdominioSlug() + "'");
        }

        Tenant tenant = new Tenant(TenantUUID.generate(),
                request.nombreEmpresa(), request.subdominioSlug(), TenantEstado.ACTIVE);

        return TenantResponse.from(tenantRepository.save(tenant));
    }
}