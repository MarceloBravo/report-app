package com.saas.report.conexion.presentation;

import com.saas.report.conexion.application.usecase.GetConexionUseCase;
import com.saas.report.conexion.application.usecase.ListConexionesUseCase;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.support.AbstractSecurityMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización de {@code /conexiones} contra la SecurityConfig real.
 *
 * <p>Cubre el requisito de que el listado de conexiones exija autenticación y
 * esté permitido a cualquier rol de plataforma o de tenant
 * ({@code PLATFORM_ADMIN}, {@code PLATFORM_USER}, {@code ADMIN} y
 * {@code VIEWER}), y también la sobrecarga con query param {@code tenantId},
 * que la regla de seguridad reserva a los roles de plataforma.
 */
@WebMvcTest(controllers = ConexionController.class)
class ConexionSecurityTest extends AbstractSecurityMvcTest {

    @MockitoBean
    private ListConexionesUseCase listConexionesUseCase;

    @MockitoBean
    private GetConexionUseCase getConexionUseCase;

    @BeforeEach
    void respuestasPorDefecto() {
        lenient().when(listConexionesUseCase.listar(any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Los roles del tenant autenticados pueden listar sus conexiones")
    void rolesDelTenantPuedenListarSusConexiones() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.ADMIN, get("/conexiones")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, get("/conexiones")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Los roles de plataforma pasan la regla de seguridad pero, al no tener tenant, el controller responde 403")
    void rolesDePlataformaSinTenantRecibenForbiddenEnElController() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN, get("/conexiones")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_USER, get("/conexiones")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Sin autenticación responde 401 y no ejecuta el caso de uso")
    void sinAutenticacionRespondeUnauthorized() throws Exception {
        mockMvc.perform(get("/conexiones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(listConexionesUseCase, never()).listar(any());
    }

    @Test
    @DisplayName("PLATFORM_ADMIN con query param tenantId lista el tenant indicado")
    void plataformaConParametroTenantIdPuedeListar() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN,
                        get("/conexiones").param("tenantId", UUID.randomUUID().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("ADMIN con query param tenantId es rechazado por la regla de seguridad")
    void rolDeTenantConParametroTenantIdEsBloqueado() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.ADMIN,
                        get("/conexiones").param("tenantId", UUID.randomUUID().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                // El mensaje viene del AccessDeniedHandler de la cadena de
                // seguridad, no del controller: así se prueba la regla nueva.
                .andExpect(jsonPath("$.message").value("Access Denied"));

        verify(listConexionesUseCase, never()).listar(any());
        verify(getConexionUseCase, never()).obtener(any(), any());
    }

    @Test
    @DisplayName("PLATFORM_ADMIN sin query param tenantId sigue respondiendo 403 en el controller")
    void plataformaSinParametroTenantIdSigueProhibida() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.PLATFORM_ADMIN, get("/conexiones")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}