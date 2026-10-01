package com.saas.report.usuarios.presentation;

import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Administración de usuarios de la plataforma.
 *
 * <p>Permite crear, listar, consultar, actualizar y eliminar usuarios. El usuario
 * que realiza cada operación se toma del contexto de seguridad
 * ({@link SecurityContextAuth}), no del cuerpo de la petición, para que el alta
 * quede auditada a nombre de quien la ejecutó.
 *
 * <p>Las rutas cuelgan de {@code /admin} y requieren los roles de plataforma
 * {@code PLATFORM_ADMIN} o {@code PLATFORM_USER}.
 */
@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final GetUsuarioUseCase getUsuarioUseCase;
    private final ListUsuariosUseCase listUsuariosUseCase;
    private final UpdateUsuarioUseCase updateUsuarioUseCase;
    private final DeleteUsuarioUseCase deleteUsuarioUseCase;

    public UsuarioController(CreateUsuarioUseCase createUsuarioUseCase,
                             GetUsuarioUseCase getUsuarioUseCase,
                             ListUsuariosUseCase listUsuariosUseCase,
                             UpdateUsuarioUseCase updateUsuarioUseCase,
                             DeleteUsuarioUseCase deleteUsuarioUseCase) {
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.getUsuarioUseCase = getUsuarioUseCase;
        this.listUsuariosUseCase = listUsuariosUseCase;
        this.updateUsuarioUseCase = updateUsuarioUseCase;
        this.deleteUsuarioUseCase = deleteUsuarioUseCase;
    }

    /**
     * Crea un usuario y le envía el email de verificación.
     *
     * @param request datos del usuario a crear
     * @return el usuario creado con estado {@code 201 CREATED}
     * @throws EmailAlreadyExistsException si el email ya está registrado
     * @throws ForbiddenException si el rol de quien llama no puede crear usuarios con
     *                            el rol solicitado
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = createUsuarioUseCase.execute(request, SecurityContextAuth.principalActual());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuario));
    }

    /**
     * Lista usuarios aplicando filtros opcionales.
     *
     * @param rol filtro por rol; {@code null} devuelve todos
     * @param activo filtro por estado; {@code null} devuelve tanto activos como inactivos
     * @return la lista de usuarios que cumplen los filtros
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> list(
            @RequestParam(required = false) UsuarioRol rol,
            @RequestParam(required = false) Boolean activo) {
        List<UsuarioResponse> response = listUsuariosUseCase.listar(rol, activo).stream()
                .map(UsuarioResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(UsuarioResponse.from(getUsuarioUseCase.execute(new UsuarioUUID(id))));
    }

    /**
     * Actualiza los datos permitidos de un usuario.
     *
     * @param id          identificador del usuario a modificar
     * @param request     campos a actualizar
     * @return el usuario ya actualizado
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws EmailAlreadyExistsException si el nuevo email ya pertenece a otro usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateUsuarioRequest request) {
        return ResponseEntity.ok(UsuarioResponse.from(updateUsuarioUseCase.execute(new UsuarioUUID(id), request)));
    }

    /**
     * Elimina un usuario.
     *
     * @param id identificador del usuario a eliminar
     * @return respuesta vacía con estado {@code 204 NO CONTENT}
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUsuarioUseCase.execute(new UsuarioUUID(id));
        return ResponseEntity.noContent().build();
    }
}