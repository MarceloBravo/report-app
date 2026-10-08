package com.saas.report.usuarios.presentation;

import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.application.dto.UsuarioResponse;
import com.saas.report.usuarios.application.usecase.CreateUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.DeleteUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.GetUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.ListUsuariosUseCase;
import com.saas.report.usuarios.application.usecase.UpdateUsuarioUseCase;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.config.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticaComoPlatformAdmin() {
        var principal = new AuthPrincipal("jefe@saas.com", "u1", null, UsuarioRol.PLATFORM_ADMIN.name());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.ADMIN, true);
    }

    private Page<UsuarioResponse> pagina(List<Usuario> usuarios, int page, int size, long total) {
        return new PageImpl<>(usuarios.stream().map(UsuarioResponse::from).toList(),
                PageRequest.of(page, size), total);
    }

    @Test
    void listaLosUsuarios() throws Exception {
        when(listUsuariosUseCase.execute(isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(pagina(List.of(usuario()), 0, 10, 1));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Ana"))
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listaLosUsuariosPaginando() throws Exception {
        when(listUsuariosUseCase.execute(isNull(), isNull(), eq(2), eq(5)))
                .thenReturn(pagina(List.of(usuario()), 2, 5, 20));

        mockMvc.perform(get("/usuarios").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    void filtraPorRolYEstado() throws Exception {
        when(listUsuariosUseCase.execute(eq("ADMIN"), eq(true), eq(0), eq(10)))
                .thenReturn(pagina(List.of(usuario()), 0, 10, 1));

        mockMvc.perform(get("/usuarios").param("rol", "ADMIN").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"));
    }

    @Test
    void devuelve400SiElEstadoNoEsBooleano() throws Exception {
        mockMvc.perform(get("/usuarios").param("activo", "quizá"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtieneUnUsuarioPorId() throws Exception {
        autenticaComoPlatformAdmin();
        Usuario usuario = usuario();
        when(getUsuarioUseCase.execute(any(UUID.class))).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/" + usuario.getUsuarioId().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@empresa.com"));
    }

    @Test
    void creaUnUsuario() throws Exception {
        autenticaComoPlatformAdmin();
        when(createUsuarioUseCase.execute(any(UsuarioRequest.class),
                any(com.saas.report.usuarios.application.dto.CreadorPor.class)))
                .thenReturn(usuario());

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + UUID.randomUUID()
                                + "\",\"nombre\":\"Ana\",\"email\":\"a@b.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void eliminaUnUsuarioYDevuelve204() throws Exception {
        autenticaComoPlatformAdmin();

        mockMvc.perform(delete("/usuarios/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}