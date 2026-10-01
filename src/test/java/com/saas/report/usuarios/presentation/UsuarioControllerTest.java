package com.saas.report.usuarios.presentation;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.application.dto.UsuarioResponse;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.verify;
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
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthPrincipal("root@saas.com", "u0", null, "PLATFORM_ADMIN"), null,
                List.of()));
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.ADMIN, true);
    }

    private Page<UsuarioResponse> pagina(List<Usuario> usuarios, Pageable pageable, long total) {
        return new PageImpl<>(usuarios.stream().map(UsuarioResponse::from).toList(), pageable, total);
    }

    @Test
    void listaLosUsuarios() throws Exception {
        when(listUsuariosUseCase.listar(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(pagina(List.of(usuario()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Ana"))
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void listaLosUsuariosPaginando() throws Exception {
        when(listUsuariosUseCase.listar(isNull(), isNull(), eq(2), eq(5)))
                .thenReturn(pagina(List.of(usuario()), PageRequest.of(2, 5), 20));

        mockMvc.perform(get("/admin/usuarios")
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
    void filtraPorRolYEstado() throws Exception {
        when(listUsuariosUseCase.listar(eq(UsuarioRol.ADMIN), eq(true), isNull(), isNull()))
                .thenReturn(pagina(List.of(usuario()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/usuarios")
                        .param("rol", "ADMIN")
                        .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"));
    }

    @Test
    void devuelve400ConRolInvalido() throws Exception {
        mockMvc.perform(get("/admin/usuarios").param("rol", "SUPERADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devuelve400SiElEstadoNoEsBooleano() throws Exception {
        mockMvc.perform(get("/admin/usuarios").param("activo", "quizá"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delegaLosParametrosAlCasoDeUso() throws Exception {
        when(listUsuariosUseCase.listar(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(pagina(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/admin/usuarios")).andExpect(status().isOk());

        verify(listUsuariosUseCase).listar(isNull(), isNull(), isNull(), isNull());
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
        autenticaComoPlatformAdmin();
        when(createUsuarioUseCase.execute(any(UsuarioRequest.class), any(AuthPrincipal.class)))
                .thenReturn(usuario());

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