package com.saas.report.suscripcion.presentation;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.plan.application.dto.PlanPrecioResponse;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.application.dto.SuscripcionResponse;
import com.saas.report.suscripcion.application.usecase.GetMiSuscripcionUseCase;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.support.AbstractSecurityMvcTest;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización de {@code /suscripcion/mia}: solo el ADMIN del tenant
 * consulta su suscripción, y el tenant se resuelve desde el principal del JWT.
 */
@WebMvcTest(controllers = MiSuscripcionController.class)
class MiSuscripcionSecurityTest extends AbstractSecurityMvcTest {

    @MockitoBean
    private GetMiSuscripcionUseCase getMiSuscripcionUseCase;

    @Test
    @DisplayName("El ADMIN de un tenant ve su suscripción con los datos de su plan")
    void adminDelTenantVeSuSuscripcion() throws Exception {
        lenient().when(getMiSuscripcionUseCase.obtenerMiSuscripcion(any())).thenReturn(respuesta());

        mockMvc.perform(autenticadoComo(UsuarioRol.ADMIN, get("/suscripcion/mia")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suscripcion.planId").value(PLAN_ID.toString()))
                .andExpect(jsonPath("$.plan.nombrePlan").value("Pro"))
                .andExpect(jsonPath("$.plan.activo").value(true));

        verify(getMiSuscripcionUseCase).obtenerMiSuscripcion(any(TenantUUID.class));
    }

    @Test
    @DisplayName("El VIEWER no ve la suscripción")
    void viewerNoVeLaSuscripcion() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, get("/suscripcion/mia")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verifyNoInteractions(getMiSuscripcionUseCase);
    }

    @Test
    @DisplayName("Los roles de plataforma tampoco ven la suscripción de un tenant")
    void rolesDePlataformaNoVenLaSuscripcion() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN, get("/suscripcion/mia")))
                .andExpect(status().isForbidden());
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, get("/suscripcion/mia")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(getMiSuscripcionUseCase);
    }

    @Test
    @DisplayName("Sin token válido responde 401")
    void sinTokenValidoRespondeUnauthorized() throws Exception {
        mockMvc.perform(get("/suscripcion/mia"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(conTokenInvalido(get("/suscripcion/mia")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conTokenExpirado(get("/suscripcion/mia")))
                .andExpect(status().isUnauthorized());

        verify(getMiSuscripcionUseCase, never()).obtenerMiSuscripcion(any());
    }

    @Test
    @DisplayName("Un ADMIN sin tenant en su principal recibe 403 y no ejecuta el caso de uso")
    void adminSinTenantRecibeForbidden() throws Exception {
        Usuario adminSinTenant = Mockito.mock(Usuario.class);
        when(adminSinTenant.getRol()).thenReturn(UsuarioRol.ADMIN);
        when(adminSinTenant.getUsuarioId()).thenReturn(UsuarioUUID.generate());
        when(adminSinTenant.getEmail()).thenReturn("sin-tenant@test.com");
        when(adminSinTenant.getTenantId()).thenReturn(null);

        mockMvc.perform(conTokenDe(adminSinTenant, get("/suscripcion/mia")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(getMiSuscripcionUseCase);
    }

    private MiSuscripcionResponse respuesta() {
        PlanPrecioResponse precio =
                new PlanPrecioResponse(UUID.randomUUID(), 1, 12, new BigDecimal("49.99"));
        PlanResponse plan =
                new PlanResponse(PLAN_ID, "Pro", "Plan profesional", 10, -1, true, true, List.of(precio));
        SuscripcionResponse suscripcion = new SuscripcionResponse(
                UUID.randomUUID(), UUID.randomUUID(), PLAN_ID,
                LocalDate.now().minusMonths(1), SuscripcionEstado.ACTIVE,
                LocalDate.now().plusMonths(1), 12, true, List.of());
        return new MiSuscripcionResponse(suscripcion, plan);
    }

    private static final UUID PLAN_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
}