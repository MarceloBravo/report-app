package com.saas.report.auth.presentation;

import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.RefreshTokenRequest;
import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.application.usecase.LoginUseCase;
import com.saas.report.auth.application.usecase.LogoutUseCase;
import com.saas.report.auth.application.usecase.RefreshTokenUseCase;
import com.saas.report.auth.application.usecase.RegisterUseCase;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Autenticación y gestión de sesión.
 *
 * <p>Cubre el login y logout con JWT, la rotación del token de refresco, el
 * registro público de usuarios y el registro desde el panel de administración
 * (donde sí se puede elegir el rol), y la consulta del usuario autenticado.
 *
 * <p>Las rutas de registro público y login son abiertas. El registro en
 * {@code /auth/admin/register} y {@code /auth/me} exigen autenticación, y la
 * primera requiere rol de plataforma.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(LoginUseCase loginUseCase,
                          RegisterUseCase registerUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    /**
     * Inicia sesión con email y contraseña.
     *
     * @param request credenciales del usuario
     * @return el par de tokens (acceso y refresco)
     * @throws InvalidCredentialsException si las credenciales no son válidas
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un usuario desde el flujo público de autoservicio.
     *
     * <p>El rol se fija en {@link UsuarioRol#VIEWER} siempre; el cliente no puede
     * elegirlo. Tras el alta se envía el email de verificación.
     *
     * @param request datos del nuevo usuario
     * @return respuesta vacía con estado {@code 201 CREATED}
     * @throws EmailAlreadyExistsException si el email ya está registrado
     * @throws ForbiddenException si quien llama no puede crear usuarios
     * @throws InvalidStatusException si el tenant del solicitante no está activo
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.registrar(request, UsuarioRol.VIEWER, SecurityContextAuth.principalActual());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Registra un usuario desde el panel de administración, indicando su rol.
     *
     * <p>Si el rol no viene en el cuerpo se asigna {@link UsuarioRol#VIEWER}. La
     * ruta exige rol de plataforma, de modo que no amplía el alcance del registro
     * público.
     *
     * @param request datos del nuevo usuario, con rol opcional
     * @return respuesta vacía con estado {@code 201 CREATED}
     * @throws EmailAlreadyExistsException si el email ya está registrado
     * @throws ForbiddenException si el rol pedido no puede ser asignado por quien llama
     * @throws InvalidStatusException si el tenant del solicitante no está activo
     */
    @PostMapping("/admin/register")
    public ResponseEntity<Void> adminRegister(@Valid @RequestBody RegisterRequest request) {
        UsuarioRol rol = request.rol() != null ? request.rol() : UsuarioRol.VIEWER;
        registerUseCase.registrar(request, rol, SecurityContextAuth.principalActual());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Renova el token de acceso a partir de un token de refresco válido.
     *
     * @param request token de refresco emitido en el login
     * @return un par de tokens nuevo
     * @throws InvalidCredentialsException si el token de refresco no viene informado
     * @throws TokenRevokedException si el token no es válido, ha expirado o ha sido revocado
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = refreshTokenUseCase.refrescar(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra la sesión revocando el token de acceso actual.
     *
     * <p>La cabecera {@code Authorization} es opcional: si no viene o no es un
     * token {@code Bearer}, la llamada responde igualmente con
     * {@code 204 NO CONTENT} sin tocar nada.
     *
     * @param authorization cabecera {@code Authorization} con el token de acceso
     * @return respuesta vacía con estado {@code 204 NO CONTENT}
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String accessToken = extractBearer(authorization);
        logoutUseCase.cerrarSesion(accessToken);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /**
     * Devuelve los datos del usuario autenticado según el token vigente.
     *
     * @return mapa con email, usuarioId, tenantId y rol
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        var principal = SecurityContextAuth.principalActual();
        return ResponseEntity.ok(Map.of(
                "email", principal.email(),
                "usuarioId", principal.usuarioId(),
                "tenantId", principal.tenantId(),
                "rol", principal.rol()));
    }

    /**
     * Extrae el token de una cabecera {@code Authorization} de tipo Bearer.
     *
     * @param authorization valor crudo de la cabecera
     * @return el token sin el prefijo, o {@code null} si la cabecera falta o no
     *         tiene el formato esperado
     */
    private String extractBearer(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return null;
    }
}