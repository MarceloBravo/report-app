package com.saas.report.consulta.presentation;

import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.usecase.ConvertirConsultaUseCase;
import com.saas.report.consulta.application.usecase.GetConsultaUseCase;
import com.saas.report.consulta.application.usecase.ListConsultasUseCase;
import com.saas.report.consulta.application.usecase.RegisterConsultaUseCase;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.config.exception.GlobalExceptionHandler;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsultaControllerTest {

    @Mock
    private RegisterConsultaUseCase registerConsultaUseCase;

    @Mock
    private ListConsultasUseCase listConsultasUseCase;

    @Mock
    private GetConsultaUseCase getConsultaUseCase;

    @Mock
    private ConvertirConsultaUseCase convertirConsultaUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConsultaController(registerConsultaUseCase, listConsultasUseCase,
                        getConsultaUseCase, convertirConsultaUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String tenantId, String usuarioId) {
        AuthPrincipal principal = new AuthPrincipal("test@test.com", usuarioId, tenantId, "ADMIN");
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                List.of(() -> "ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticate() {
        authenticate(TenantUUID.generate().getValue().toString(), UUID.randomUUID().toString());
    }

    private ConsultaResponse response() {
        return new ConsultaResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "¿Cuántas ventas?", "SELECT 1;", LocalDateTime.now(), true, 5, null, 3, List.of());
    }

    private String bodyRegistro() {
        return "{\"tenantId\":\"" + UUID.randomUUID() + "\",\"usuarioId\":\"" + UUID.randomUUID()
                + "\",\"preguntaUsuario\":\"¿Cuántas ventas?\",\"querySqlGenerada\":\"SELECT 1;\"}";
    }

    private String bodyConvertir() {
        return "{\"preguntaUsuario\":\"¿Cuántas ventas?\"}";
    }

    @Test
    void convierteConsultaNLASqlYDevuelve200() throws Exception {
        UUID tenantId = TenantUUID.generate().getValue();
        UUID usuarioId = UUID.randomUUID();
        authenticate(tenantId.toString(), usuarioId.toString());
        when(convertirConsultaUseCase.convertir(any(UUID.class), any(UUID.class),
                any(ConvertirConsultaRequest.class))).thenReturn(response());

        mockMvc.perform(post("/consultas/convertir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyConvertir()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.querySqlGenerada").value("SELECT 1;"));

        ArgumentCaptor<UUID> tenantCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> usuarioCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<ConvertirConsultaRequest> requestCaptor =
                ArgumentCaptor.forClass(ConvertirConsultaRequest.class);
        verify(convertirConsultaUseCase).convertir(tenantCaptor.capture(), usuarioCaptor.capture(),
                requestCaptor.capture());
        assertThat(tenantCaptor.getValue()).isEqualTo(tenantId);
        assertThat(usuarioCaptor.getValue()).isEqualTo(usuarioId);
        assertThat(requestCaptor.getValue().preguntaUsuario()).isEqualTo("¿Cuántas ventas?");
        assertThat(requestCaptor.getValue().dbConnectionId()).isNull();
    }

    @Test
    void devuelve400SiLaPreguntaDeConversionEstaVacia() throws Exception {
        authenticate();

        mockMvc.perform(post("/consultas/convertir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preguntaUsuario\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve403EnConversionSiNoHaySesion() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/consultas/convertir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyConvertir()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void registraConsultaYDevuelve201() throws Exception {
        when(registerConsultaUseCase.registrar(any(ConsultaRequest.class))).thenReturn(response());

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRegistro()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preguntaUsuario").value("¿Cuántas ventas?"));
    }

    @Test
    void devuelve400SiFaltanCamposObligatorios() throws Exception {
        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve429SiSeSuperaElLimiteDelPlan() throws Exception {
        when(registerConsultaUseCase.registrar(any(ConsultaRequest.class)))
                .thenThrow(new LimiteConsultasExcedidoException("Límite excedido"));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRegistro()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void obtieneYListaConsultas() throws Exception {
        UUID id = UUID.randomUUID();
        when(getConsultaUseCase.obtener(any())).thenReturn(response());
        when(listConsultasUseCase.listar(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/consultas/{consultaId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultasEnPeriodo").value(3));

        mockMvc.perform(get("/consultas").param("tenantId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].consultasEnPeriodo").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.pageable").doesNotExist());
    }

    @Test
    void listaElHistorialPaginando() throws Exception {
        when(listConsultasUseCase.listar(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(2, 5), 20));

        mockMvc.perform(get("/consultas")
                        .param("tenantId", UUID.randomUUID().toString())
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void delegaTenantPageYSizeAlCasoDeUso() throws Exception {
        when(listConsultasUseCase.listar(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/consultas").param("tenantId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());

        verify(listConsultasUseCase).listar(any(TenantUUID.class), isNull(), isNull());
    }

    @Test
    void devuelve400SiFaltaElTenantId() throws Exception {
        mockMvc.perform(get("/consultas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Falta el parámetro obligatorio 'tenantId'"));
    }
}