package com.saas.report.onboarding.presentation;

import com.saas.report.onboarding.application.dto.OnboardingRequest;
import com.saas.report.onboarding.application.dto.OnboardingResponse;
import com.saas.report.onboarding.application.usecase.OnboardClienteUseCase;
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OnboardingControllerTest {

    @Mock
    private OnboardClienteUseCase onboardClienteUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OnboardingController(onboardClienteUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private String jsonValido() {
        return "{\"cliente\":{\"nombreEmpresa\":\"Acme\",\"subdominioSlug\":\"acme\"},"
                + "\"usuario\":{\"nombre\":\"Juan\",\"email\":\"juan@acme.com\",\"password\":\"password123\"},"
                + "\"conexion\":{\"host\":\"db.acme.com\",\"puerto\":5432,\"dbName\":\"ventas\","
                + "\"dbUser\":\"report_ro\",\"dbPassword\":\"secreto\",\"schemaName\":\"public\"}}";
    }

    @Test
    void registraClienteYDevuelve201() throws Exception {
        when(onboardClienteUseCase.registrar(any(OnboardingRequest.class)))
                .thenReturn(new OnboardingResponse(UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), UUID.randomUUID(),
                        "http://localhost:5173/verificar-email?token=abc"));

        mockMvc.perform(post("/self-serve/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.dbConnectionId").isNotEmpty())
                .andExpect(jsonPath("$.emailVerificacionUrl").isNotEmpty());
    }

    @Test
    void rechazaSubdominioInvalido() throws Exception {
        mockMvc.perform(post("/self-serve/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido().replace("\"acme\"", "\"ACME!\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rechazaPasswordCorta() throws Exception {
        mockMvc.perform(post("/self-serve/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido().replace("\"password123\"", "\"abc\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void devuelve409SiElSubdominioYaExiste() throws Exception {
        when(onboardClienteUseCase.registrar(any(OnboardingRequest.class)))
                .thenThrow(new TenantSlugAlreadyExistsException("El subdominio ya existe"));

        mockMvc.perform(post("/self-serve/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void devuelve409SiElEmailYaExiste() throws Exception {
        when(onboardClienteUseCase.registrar(any(OnboardingRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("El email ya está registrado"));

        mockMvc.perform(post("/self-serve/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}