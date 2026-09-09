package com.saas.report.usuarios.presentation;

import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.application.usecase.CreateUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.DeleteUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.GetUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.ListUsuariosUseCase;
import com.saas.report.usuarios.application.usecase.UpdateUsuarioUseCase;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.presentation.rest.GlobalExceptionHandler;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private CreateUsuarioUseCase createUsuarioUseCase;

    @Mock
    private GetUsuarioUseCase getUsuarioUseCase;

    @Mock
    private ListUsuariosUseCase listUsuariosUseCase;

    @Mock
    private UpdateUsuarioUseCase updateUsuarioUseCase;

    @Mock
    private DeleteUsuarioUseCase deleteUsuarioUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsuarioController(createUsuarioUseCase, getUsuarioUseCase,
                        listUsuariosUseCase, updateUsuarioUseCase, deleteUsuarioUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.ADMIN, true);
    }

    @Test
    void listaLosUsuarios() throws Exception {
        when(listUsuariosUseCase.listar(isNull(), isNull())).thenReturn(List.of(usuario()));

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"))
                .andExpect(jsonPath("$[0].rol").value("ADMIN"));
    }

    @Test
    void obtieneUnUsuarioPorId() throws Exception {
        Usuario usuario = usuario();
        when(getUsuarioUseCase.execute(any(UsuarioUUID.class))).thenReturn(usuario);

        mockMvc.perform(get("/admin/usuarios/" + usuario.getUsuarioId().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@empresa.com"));
    }

    @Test
    void creaUnUsuarioYDevuelve201() throws Exception {
        when(createUsuarioUseCase.execute(any(UsuarioRequest.class))).thenReturn(usuario());

        mockMvc.perform(post("/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + UUID.randomUUID()
                                + "\",\"nombre\":\"Ana\",\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void eliminaUnUsuarioYDevuelve204() throws Exception {
        mockMvc.perform(delete("/admin/usuarios/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}