package com.saas.report.suscripcion.presentation;

import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.suscripcion.application.dto.ActualizarPlanRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionRequest;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse.SuscripcionHistorialPrecioResponse;
import com.saas.report.suscripcion.application.usecase.CancelSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.CreateSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.SuspendSuscripcionUseCase;
import com.saas.report.suscripcion.application.usecase.UpdateSuscripcionPlanUseCase;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.exception.SuscripcionAlreadyActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SuscripcionControllerTest {

    @Mock
    private CreateSuscripcionUseCase createSuscripcionUseCase;

    @Mock
    private CancelSuscripcionUseCase cancelSuscripcionUseCase;

    @Mock
    private SuspendSuscripcionUseCase suspendSuscripcionUseCase;

    @Mock
    private UpdateSuscripcionPlanUseCase updateSuscripcionPlanUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SuscripcionController(createSuscripcionUseCase, cancelSuscripcionUseCase,
                        suspendSuscripcionUseCase, updateSuscripcionPlanUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private SuscripcionResponse response() {
        return new SuscripcionResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1), 0, true,
                List.of(new SuscripcionHistorialPrecioResponse(UUID.randomUUID(), LocalDate.now(), null,
                        new BigDecimal("9.90"), UUID.randomUUID(), true)));
    }

    @Test
    void creaSuscripcionYDevuelve201() throws Exception {
        when(createSuscripcionUseCase.crear(any(SuscripcionRequest.class))).thenReturn(response());

        mockMvc.perform(post("/admin/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + UUID.randomUUID() + "\",\"planId\":\""
                                + UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVE"));
    }

    @Test
    void devuelve400SiFaltanCamposObligatorios() throws Exception {
        mockMvc.perform(post("/admin/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve409SiElTenantYaTieneSuscripcion() throws Exception {
        when(createSuscripcionUseCase.crear(any(SuscripcionRequest.class)))
                .thenThrow(new SuscripcionAlreadyActiveException("Ya tiene suscripción"));

        mockMvc.perform(post("/admin/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + UUID.randomUUID() + "\",\"planId\":\""
                                + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void cancelaSuspendeYActualizaPlan() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelSuscripcionUseCase.cancelar(any())).thenReturn(response());
        when(suspendSuscripcionUseCase.suspender(any())).thenReturn(response());
        when(updateSuscripcionPlanUseCase.actualizarPlan(any(), any(ActualizarPlanRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/admin/suscripciones/{id}/cancelar", id)).andExpect(status().isOk());
        mockMvc.perform(post("/admin/suscripciones/{id}/suspender", id)).andExpect(status().isOk());
        mockMvc.perform(put("/admin/suscripciones/{id}/plan", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVE"));
    }
}