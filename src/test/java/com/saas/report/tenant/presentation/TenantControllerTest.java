package com.saas.report.tenant.presentation;

import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.tenant.application.dto.TenantRequest;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.application.usecase.ActivateTenantUseCase;
import com.saas.report.tenant.application.usecase.CancelTenantUseCase;
import com.saas.report.tenant.application.usecase.CreateTenantUseCase;
import com.saas.report.tenant.application.usecase.GetTenantUseCase;
import com.saas.report.tenant.application.usecase.SuspendTenantUseCase;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private CreateTenantUseCase createTenantUseCase;

    @Mock
    private GetTenantUseCase getTenantUseCase;

    @Mock
    private ActivateTenantUseCase activateTenantUseCase;

    @Mock
    private SuspendTenantUseCase suspendTenantUseCase;

    @Mock
    private CancelTenantUseCase cancelTenantUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TenantController(createTenantUseCase, getTenantUseCase,
                        activateTenantUseCase, suspendTenantUseCase, cancelTenantUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private TenantResponse response() {
        return new TenantResponse(UUID.randomUUID(), "Acme", "acme", TenantEstado.ACTIVE, LocalDateTime.now());
    }

    @Test
    void creaTenantYDevuelve201() throws Exception {
        when(createTenantUseCase.crear(any(TenantRequest.class))).thenReturn(response());

        mockMvc.perform(post("/admin/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreEmpresa\":\"Acme\",\"subdominioSlug\":\"acme\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subdominioSlug").value("acme"));
    }

    @Test
    void rechazaSubdominioInvalido() throws Exception {
        mockMvc.perform(post("/admin/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreEmpresa\":\"Acme\",\"subdominioSlug\":\"ACME!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve409SiElSubdominioYaExiste() throws Exception {
        when(createTenantUseCase.crear(any(TenantRequest.class)))
                .thenThrow(new TenantSlugAlreadyExistsException("El subdominio ya existe"));

        mockMvc.perform(post("/admin/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreEmpresa\":\"Acme\",\"subdominioSlug\":\"acme\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
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
}