package com.saas.report.tenant.application.usecase;

import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.domain.Tenant;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTenantUseCase {
    
    private final TenantRepository tenantRepository;

    public ListTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }
    

    @Transactional(readOnly = true)
    public  Page<TenantResponse> list(String searchTextString, Integer page, Integer size) {
        Page<Tenant> tenants = tenantRepository.findAll(searchTextString, page, size);
        return tenants.map(TenantResponse::from);
    }
    
}
