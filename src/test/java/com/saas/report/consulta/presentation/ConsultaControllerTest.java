package com.saas.report.consulta.presentation;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.application.usecase.GetConsultaUseCase;
import com.saas.report.consulta.application.usecase.ListConsultasUseCase;
import com.saas.report.consulta.application.usecase.RegisterConsultaUseCase;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConsultaController(registerConsultaUseCase, listConsultasUseCase,
                        getConsultaUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private ConsultaResponse response() {
        return new ConsultaResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "¿Cuántas ventas?", "SELECT 1;", LocalDateTime.now(), true, 5, null, 3);
    }

    private String bodyRegistro() {
        return "{\"tenantId\":\"" + UUID.randomUUID() + "\",\"usuarioId\":\"" + UUID.randomUUID()
                + "\",\"preguntaUsuario\":\"¿Cuántas ventas?\",\"querySqlGenerada\":\"SELECT 1;\"}";
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
        when(listConsultasUseCase.listar(any())).thenReturn(List.of(response()));

        mockMvc.perform(get("/consultas/{consultaId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultasEnPeriodo").value(3));

        mockMvc.perform(get("/consultas").param("tenantId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consultasEnPeriodo").value(3));
    }
}