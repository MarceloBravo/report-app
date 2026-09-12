package com.saas.report.usuarios.presentation;

import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.usuarios.application.usecase.VerificarEmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailVerificacionControllerTest {

    @Mock
    private VerificarEmailUseCase verificarEmailUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EmailVerificacionController(verificarEmailUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void devuelve200CuandoElTokenEsValido() throws Exception {
        mockMvc.perform(get("/self-serve/verificar-email").param("token", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void devuelve400CuandoElTokenEsInvalido() throws Exception {
        doThrow(new IllegalArgumentException("Token de verificación inválido"))
                .when(verificarEmailUseCase).verificar("token-malo");

        mockMvc.perform(get("/self-serve/verificar-email").param("token", "token-malo"))
                .andExpect(status().isBadRequest());
    }
}