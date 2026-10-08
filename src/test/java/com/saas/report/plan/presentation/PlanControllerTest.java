package com.saas.report.plan.presentation;

import com.saas.report.plan.application.dto.PlanPrecioResponse;
import com.saas.report.plan.application.dto.PlanRequest;
import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.application.usecase.ActivatePlanUseCase;
import com.saas.report.plan.application.usecase.CreatePlanUseCase;
import com.saas.report.plan.application.usecase.DeactivatePlanUseCase;
import com.saas.report.plan.application.usecase.GetPlanUseCase;
import com.saas.report.plan.application.usecase.ListPlansUseCase;
import com.saas.report.plan.domain.exception.PlanNombreAlreadyExistsException;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.config.exception.GlobalExceptionHandler;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    @Mock
    private CreatePlanUseCase createPlanUseCase;

    @Mock
    private GetPlanUseCase getPlanUseCase;

    @Mock
    private ListPlansUseCase listPlansUseCase;

    @Mock
    private ActivatePlanUseCase activatePlanUseCase;

    @Mock
    private DeactivatePlanUseCase deactivatePlanUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PlanController(createPlanUseCase, getPlanUseCase, listPlansUseCase,
                        activatePlanUseCase, deactivatePlanUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private PlanResponse response() {
        return new PlanResponse(UUID.randomUUID(), "PRO", "Plan pro", 20, 1000, true, false,
                List.of(new PlanPrecioResponse(UUID.randomUUID(), 1, null, new BigDecimal("10.00"))));
    }

    private PlanResponse response(Boolean activo) {
        return new PlanResponse(UUID.randomUUID(), "PRO", "Plan pro", 20, 1000, activo, false,
                List.of(new PlanPrecioResponse(UUID.randomUUID(), 1, null, new BigDecimal("10.00"))));
    }

    @Test
    void creaPlanYDevuelve201() throws Exception {
        when(createPlanUseCase.crear(any(PlanRequest.class))).thenReturn(response());

        mockMvc.perform(post("/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePlan\":\"PRO\",\"descripcion\":\"Plan pro\",\"maxUsuarios\":20,"
                                + "\"limiteConsultasMensuales\":1000,\"activo\":true,"
                                + "\"precios\":[{\"mesDesde\":1,\"mesHasta\":null,\"precio\":10.00}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombrePlan").value("PRO"));
    }

    @Test
    void devuelve400SiFaltanCamposObligatorios() throws Exception {
        mockMvc.perform(post("/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve409SiElNombreYaExiste() throws Exception {
        when(createPlanUseCase.crear(any(PlanRequest.class)))
                .thenThrow(new PlanNombreAlreadyExistsException("El nombre del plan ya existe"));

        mockMvc.perform(post("/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePlan\":\"PRO\",\"descripcion\":\"Plan pro\",\"maxUsuarios\":20,"
                                + "\"limiteConsultasMensuales\":1000,\"activo\":true,"
                                + "\"precios\":[{\"mesDesde\":1,\"precio\":10.00}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void obtieneYListaPlanes() throws Exception {
        when(getPlanUseCase.obtener(any())).thenReturn(response());
        when(listPlansUseCase.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/admin/plans/{planId}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombrePlan").value("PRO"));

        mockMvc.perform(get("/admin/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombrePlan").value("PRO"));
    }

    @Test
    void activaElPlanYDevuelve200() throws Exception {
        when(activatePlanUseCase.activar(any())).thenReturn(response(true));

        mockMvc.perform(post("/admin/plans/{planId}/activar", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void desactivaElPlanYDevuelve200() throws Exception {
        when(deactivatePlanUseCase.desactivar(any())).thenReturn(response(false));

        mockMvc.perform(post("/admin/plans/{planId}/desactivar", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void devuelve400SiNoSePuedeCambiarElEstadoDelPlan() throws Exception {
        when(deactivatePlanUseCase.desactivar(any()))
                .thenThrow(new InvalidStatusException("El plan gratuito no se puede desactivar"));

        mockMvc.perform(post("/admin/plans/{planId}/desactivar", UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}