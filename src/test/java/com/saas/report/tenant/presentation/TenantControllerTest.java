package com.saas.report.tenant.presentation;

import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.application.usecase.ActivateTenantUseCase;
import com.saas.report.tenant.application.usecase.CancelTenantUseCase;
import com.saas.report.tenant.application.usecase.GetTenantUseCase;
import com.saas.report.tenant.application.usecase.ListTenantUseCase;
import com.saas.report.tenant.application.usecase.SuspendTenantUseCase;
import com.saas.report.tenant.domain.TenantEstado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private GetTenantUseCase getTenantUseCase;

    @Mock
    private ActivateTenantUseCase activateTenantUseCase;

    @Mock
    private SuspendTenantUseCase suspendTenantUseCase;

    @Mock
    private CancelTenantUseCase cancelTenantUseCase;

    @Mock
    private ListTenantUseCase listTenantUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TenantController(getTenantUseCase,
                        activateTenantUseCase, suspendTenantUseCase, cancelTenantUseCase, listTenantUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private TenantResponse response() {
        return new TenantResponse(UUID.randomUUID(), "Acme", "acme", TenantEstado.ACTIVE, LocalDateTime.now());
    }

    @Test
    void obtieneYCambiaDeEstado() throws Exception {
        UUID id = UUID.randomUUID();
        when(getTenantUseCase.obtener(any())).thenReturn(response());
        when(activateTenantUseCase.activar(any())).thenReturn(response());
        when(suspendTenantUseCase.suspender(any())).thenReturn(response());
        when(cancelTenantUseCase.cancelar(any())).thenReturn(response());

        mockMvc.perform(get("/admin/tenants/{tenantId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreEmpresa").value("Acme"));

        mockMvc.perform(post("/admin/tenants/{tenantId}/activate", id)).andExpect(status().isOk());
        mockMvc.perform(post("/admin/tenants/{tenantId}/suspend", id)).andExpect(status().isOk());
        mockMvc.perform(post("/admin/tenants/{tenantId}/cancel", id)).andExpect(status().isOk());
    }

    @Test
    void listaSinSearchText() throws Exception {
        when(listTenantUseCase.list(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/tenants/{page}/{size}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreEmpresa").value("Acme"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.pageable").doesNotExist());

        verify(listTenantUseCase).list(null, 0, 10);
    }

    @Test
    void listaConSearchText() throws Exception {
        when(listTenantUseCase.list(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 15));

        mockMvc.perform(get("/admin/tenants/{page}/{size}/{searchTextString}", 0, 10, "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        verify(listTenantUseCase).list("acme", 0, 10);
    }
}