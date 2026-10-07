package com.saas.report.conexion.presentation;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.conexion.application.dto.ConexionDetailResponse;
import com.saas.report.conexion.application.dto.ConexionListResponse;
import com.saas.report.conexion.application.usecase.GetConexionUseCase;
import com.saas.report.conexion.application.usecase.ListConexionesUseCase;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del controller de conexiones con MockMvc aislado y el tenant
 * resuelto del contexto de seguridad.
 */
@ExtendWith(MockitoExtension.class)
class ConexionControllerTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID OTRO_TENANT_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final UUID CONEXION_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Mock
    private ListConexionesUseCase listConexionesUseCase;

    @Mock
    private GetConexionUseCase getConexionUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConexionController(listConexionesUseCase, getConexionUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        autenticarComoTenant();
    }

    @AfterEach
    void limpiaContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComoTenant() {
        AuthPrincipal principal = new AuthPrincipal("prueba@test.com", "usuario-1", TENANT_ID.toString(), "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private void autenticarComoPlataforma() {
        AuthPrincipal principal = new AuthPrincipal("plataforma@test.com", "usuario-2", null, "PLATFORM_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void listaLasConexionesDelTenant() throws Exception {
        when(listConexionesUseCase.listar(any(TenantUUID.class)))
                .thenReturn(List.of(new ConexionListResponse(CONEXION_ID, "ventas", "public")));

        mockMvc.perform(get("/conexiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dbConnectionId").value(CONEXION_ID.toString()))
                .andExpect(jsonPath("$[0].dbName").value("ventas"))
                .andExpect(jsonPath("$[0].schemaName").value("public"));

        verify(listConexionesUseCase).listar(new TenantUUID(TENANT_ID));
    }

    @Test
    void devuelveElDetalleDeUnaConexion() throws Exception {
        when(getConexionUseCase.obtener(any(TenantUUID.class), any(TenantDbConnectionUUID.class)))
                .thenReturn(new ConexionDetailResponse(CONEXION_ID, "db.acme.com", "ventas", "public"));

        mockMvc.perform(get("/conexiones/" + CONEXION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dbConnectionId").value(CONEXION_ID.toString()))
                .andExpect(jsonPath("$.host").value("db.acme.com"))
                .andExpect(jsonPath("$.dbName").value("ventas"))
                .andExpect(jsonPath("$.schemaName").value("public"));

        verify(getConexionUseCase).obtener(new TenantUUID(TENANT_ID), new TenantDbConnectionUUID(CONEXION_ID));
    }

    @Test
    void devuelve404SiLaConexionNoExisteOEsDeOtroTenant() throws Exception {
        when(getConexionUseCase.obtener(any(TenantUUID.class), any(TenantDbConnectionUUID.class)))
                .thenThrow(new ResourceNotFoundException("No se encontró la conexión " + CONEXION_ID));

        mockMvc.perform(get("/conexiones/" + CONEXION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void devuelve403SinAutenticacion() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/conexiones"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(listConexionesUseCase, getConexionUseCase);
    }

    @Test
    void devuelve404SiElIdentificadorNoEsUnUuid() throws Exception {
        mockMvc.perform(get("/conexiones/no-es-un-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verifyNoInteractions(getConexionUseCase);
    }

    @Test
    void listaLasConexionesDelTenantIndicadoSiElRolEsDePlataforma() throws Exception {
        autenticarComoPlataforma();
        when(listConexionesUseCase.listar(any(TenantUUID.class)))
                .thenReturn(List.of(new ConexionListResponse(CONEXION_ID, "ventas", "public")));

        mockMvc.perform(get("/conexiones").param("tenantId", OTRO_TENANT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dbConnectionId").value(CONEXION_ID.toString()));

        verify(listConexionesUseCase).listar(new TenantUUID(OTRO_TENANT_ID));
        verifyNoInteractions(getConexionUseCase);
    }

    @Test
    void devuelve403SiUnRolDeTenantEnviaElParametroTenantId() throws Exception {
        mockMvc.perform(get("/conexiones").param("tenantId", OTRO_TENANT_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value("El parámetro tenantId solo puede usarse con roles de plataforma"));

        verifyNoInteractions(listConexionesUseCase, getConexionUseCase);
    }

    @Test
    void devuelveElDetalleDeLaConexionDelTenantIndicadoSiElRolEsDePlataforma() throws Exception {
        autenticarComoPlataforma();
        when(getConexionUseCase.obtener(any(TenantUUID.class), any(TenantDbConnectionUUID.class)))
                .thenReturn(new ConexionDetailResponse(CONEXION_ID, "db.acme.com", "ventas", "public"));

        mockMvc.perform(get("/conexiones/" + CONEXION_ID).param("tenantId", OTRO_TENANT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dbConnectionId").value(CONEXION_ID.toString()))
                .andExpect(jsonPath("$.host").value("db.acme.com"));

        verify(getConexionUseCase).obtener(new TenantUUID(OTRO_TENANT_ID),
                new TenantDbConnectionUUID(CONEXION_ID));
        verifyNoInteractions(listConexionesUseCase);
    }

    @Test
    void devuelve404SiElParametroTenantIdNoEsUnUuid() throws Exception {
        autenticarComoPlataforma();

        mockMvc.perform(get("/conexiones").param("tenantId", "no-es-un-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No se encontró el tenant no-es-un-uuid"));

        verifyNoInteractions(listConexionesUseCase, getConexionUseCase);
    }

    @Test
    void devuelve403SiUnRolDeTenantEnviaElParametroTenantIdEnElDetalle() throws Exception {
        mockMvc.perform(get("/conexiones/" + CONEXION_ID).param("tenantId", OTRO_TENANT_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(listConexionesUseCase, getConexionUseCase);
    }
}