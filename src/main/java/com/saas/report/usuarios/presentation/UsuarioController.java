package com.saas.report.usuarios.presentation;

import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.usuarios.application.dto.CreadorPor;
import com.saas.report.usuarios.application.dto.UpdateUsuarioRequest;
import com.saas.report.usuarios.application.dto.UsuarioRequest;
import com.saas.report.usuarios.application.dto.UsuarioResponse;
import com.saas.report.usuarios.application.dto.UsuariosPageResponse;
import com.saas.report.usuarios.application.usecase.CreateUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.DeleteUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.GetUsuarioUseCase;
import com.saas.report.usuarios.application.usecase.ListUsuariosUseCase;
import com.saas.report.usuarios.application.usecase.UpdateUsuarioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
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

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PLATFORM_USER')")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        var p = SecurityContextAuth.principalActual();
        var creador = new CreadorPor(p.email(), p.rol());
        return ResponseEntity.ok(UsuarioResponse.from(createUsuarioUseCase.execute(request, creador)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PLATFORM_USER','ADMIN','VIEWER')")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable UUID id) {
        var p = SecurityContextAuth.principalActual();
        var rolActual = p.rol();
        if ("VIEWER".equals(rolActual)) {
            throw new ForbiddenException("El rol VIEWER no puede consultar otros usuarios");
        }
        if ("ADMIN".equals(rolActual)) {
            var usuario = getUsuarioUseCase.execute(id);
            if (usuario.getTenantId() == null || !p.tenantId().equals(usuario.getTenantId().getValue().toString())) {
                throw new ForbiddenException("No autorizado para consultar usuarios de otro tenant");
            }
            return ResponseEntity.ok(UsuarioResponse.from(usuario));
        }
        return ResponseEntity.ok(UsuarioResponse.from(getUsuarioUseCase.execute(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PLATFORM_USER','ADMIN','VIEWER')")
    public ResponseEntity<UsuariosPageResponse> listarUsuarios(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(required = false) String rol,
                                                              @RequestParam(required = false) Boolean activo) {        
        return ResponseEntity.ok(UsuariosPageResponse.from(listUsuariosUseCase.execute(rol, activo, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PLATFORM_USER','ADMIN')")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable UUID id,
                                                             @Valid @RequestBody UpdateUsuarioRequest request) {
        var p = SecurityContextAuth.principalActual();
        var creador = new CreadorPor(p.email(), p.rol());
        return ResponseEntity.ok(UsuarioResponse.from(updateUsuarioUseCase.execute(id, request, creador)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PLATFORM_USER')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable UUID id) {
        var p = SecurityContextAuth.principalActual();
        var creador = new CreadorPor(p.email(), p.rol());
        deleteUsuarioUseCase.execute(id, creador);
        return ResponseEntity.noContent().build();
    }
}