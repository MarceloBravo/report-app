package com.saas.report.plan.presentation;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.plan.application.dto.PlanPrecioResponse;
import com.saas.report.plan.application.dto.PlanRequest;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.application.usecase.ActivatePlanUseCase;
import com.saas.report.plan.application.usecase.CreatePlanUseCase;
import com.saas.report.plan.application.usecase.DeactivatePlanUseCase;
import com.saas.report.plan.application.usecase.GetPlanUseCase;
import com.saas.report.plan.application.usecase.ListPlansUseCase;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.support.AbstractSecurityMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización de {@code /admin/plans} contra la SecurityConfig real.
 *
 * <p>Cubre el requisito de que activar y desactivar planes quede reservado a
 * PLATFORM_ADMIN sin quitarle a PLATFORM_USER el resto de la gestión de planes.
 */
@WebMvcTest(controllers = PlanController.class)
class PlanSecurityTest extends AbstractSecurityMvcTest {

    private static final UUID PLAN_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final PlanUUID PLAN_UUID = new PlanUUID(PLAN_ID);
    private static final String ACTIVAR = "/admin/plans/" + PLAN_ID + "/activar";
    private static final String DESACTIVAR = "/admin/plans/" + PLAN_ID + "/desactivar";

    @MockitoBean
    private CreatePlanUseCase createPlanUseCase;

    @MockitoBean
    private GetPlanUseCase getPlanUseCase;

    @MockitoBean
    private ListPlansUseCase listPlansUseCase;

    @MockitoBean
    private ActivatePlanUseCase activatePlanUseCase;

    @MockitoBean
    private DeactivatePlanUseCase deactivatePlanUseCase;

    @BeforeEach
    void respuestasPorDefecto() {
        lenient().when(activatePlanUseCase.activar(PLAN_UUID)).thenReturn(plan(true));
        lenient().when(deactivatePlanUseCase.desactivar(PLAN_UUID)).thenReturn(plan(false));
        lenient().when(listPlansUseCase.listar()).thenReturn(List.of(plan(true)));
        lenient().when(getPlanUseCase.obtener(PLAN_UUID)).thenReturn(plan(true));
        lenient().when(createPlanUseCase.crear(any(PlanRequest.class))).thenReturn(plan(true));
    }

    @Test
    @DisplayName("PLATFORM_ADMIN sí puede activar y desactivar planes")
    void platformAdminGestionaElEstadoDeLosPlanes() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN, post(ACTIVAR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN, post(DESACTIVAR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        verify(activatePlanUseCase).activar(PLAN_UUID);
        verify(deactivatePlanUseCase).desactivar(PLAN_UUID);
    }

    @Test
    @DisplayName("PLATFORM_USER puede consultar y crear planes, pero no cambiar su estado")
    void platformUserNoCambiaElEstadoDeLosPlanes() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, post(ACTIVAR)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, post(DESACTIVAR)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(activatePlanUseCase, deactivatePlanUseCase);
    }

    @Test
    @DisplayName("PLATFORM_USER conserva el acceso al resto de /admin/plans")
    void platformUserConservaLaGestionDePlanes() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, get("/admin/plans")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombrePlan").value("Pro"));

        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, post("/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombrePlan": "Pro",
                                  "descripcion": "Plan profesional",
                                  "maxUsuarios": 10,
                                  "limiteConsultasMensuales": -1,
                                  "activo": true,
                                  "precios": [{"mesDesde": 1, "mesHasta": 12, "precio": 49.99}]
                                }
                                """)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Los roles del tenant no entran a /admin/plans")
    void rolesDelTenantNoEntranAAdminPlanes() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.ADMIN, post(ACTIVAR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, post(DESACTIVAR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, get("/admin/plans")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(activatePlanUseCase, deactivatePlanUseCase, listPlansUseCase);
    }

    @Test
    @DisplayName("Un token que no autentica recibe 401 con el cuerpo de error estandarizado")
    void sinTokenValidoRespondeUnauthorized() throws Exception {
        mockMvc.perform(post(ACTIVAR))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        mockMvc.perform(conTokenInvalido(post(DESACTIVAR)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conTokenExpirado(post(ACTIVAR)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conTokenRevocado(post(DESACTIVAR)))
                .andExpect(status().isUnauthorized());

        verify(activatePlanUseCase, never()).activar(any());
        verify(deactivatePlanUseCase, never()).desactivar(any());
    }

    private PlanResponse plan(boolean activo) {
        PlanPrecioResponse precio =
                new PlanPrecioResponse(UUID.randomUUID(), 1, 12, new BigDecimal("49.99"));
        return new PlanResponse(PLAN_ID, "Pro", "Plan profesional", 10, -1, activo, true, List.of(precio));
    }
}