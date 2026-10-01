package com.saas.report.suscripcion.presentation;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse.SuscripcionHistorialPrecioResponse;
import com.saas.report.suscripcion.application.usecase.GetMiSuscripcionUseCase;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MiSuscripcionControllerTest {

    @Mock
    private GetMiSuscripcionUseCase getMiSuscripcionUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MiSuscripcionController(getMiSuscripcionUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MiSuscripcionResponse response() {
        SuscripcionResponse suscripcion = new SuscripcionResponse(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), LocalDate.now(), SuscripcionEstado.ACTIVE,
                LocalDate.now().plusMonths(1), 0, true,
                List.of(new SuscripcionHistorialPrecioResponse(UUID.randomUUID(), LocalDate.now(), null,
                        new BigDecimal("9.90"), UUID.randomUUID(), true)));
        PlanResponse plan = new PlanResponse(suscripcion.planId(), "Plan Básico", "Plan gratuito",
                1, 100, true, false, List.of());
        return new MiSuscripcionResponse(suscripcion, plan);
    }

    private void authenticate(String tenantId) {
        AuthPrincipal principal = new AuthPrincipal("test@test.com", UUID.randomUUID().toString(),
                tenantId, "ADMIN");
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                List.of(() -> "ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void devuelve200ConSuscripcionYPlanDelTenant() throws Exception {
        when(getMiSuscripcionUseCase.obtenerMiSuscripcion(any(TenantUUID.class))).thenReturn(response());
        authenticate(TenantUUID.generate().getValue().toString());

        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suscripcion.estado").value("ACTIVE"))
                .andExpect(jsonPath("$.plan.nombrePlan").value("Plan Básico"));
    }

    @Test
    void devuelve403CuandoNoHayTenantId() throws Exception {
        authenticate(null);

        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelve403CuandoTenantIdNoEsValido() throws Exception {
        authenticate("invalid-uuid");

        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelve403CuandoNoHayAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelve404CuandoElTenantNoTieneSuscripcion() throws Exception {
        when(getMiSuscripcionUseCase.obtenerMiSuscripcion(any(TenantUUID.class)))
                .thenThrow(new ResourceNotFoundException("El tenant no tiene una suscripción registrada"));
        authenticate(TenantUUID.generate().getValue().toString());

        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isNotFound());
    }
}