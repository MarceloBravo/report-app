package com.saas.report.auth.presentation;

import com.saas.report.auth.application.AuthCookieService;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.RefreshTokenRequest;
import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.application.dto.SessionResponse;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Autenticación y gestión de sesión.
 *
 * <p>Cubre el login y logout con JWT, la rotación del token de refresco, el
 * registro público de usuarios y el registro desde el panel de administración
 * (donde sí se puede elegir el rol), y la consulta del usuario autenticado.
 *
 * <p>Los tokens viajan en cookies {@code HttpOnly} ({@code access_token} y
 * {@code refresh_token}) para que el frontend no tenga que almacenarlos en
 * JavaScript. El login acepta {@code ?raw=true} para obtener los tokens en el
 * cuerpo cuando el cliente no es un navegador (Swagger, curl, Postman).
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
    private final AuthCookieService cookieService;

    public AuthController(LoginUseCase loginUseCase,
                          RegisterUseCase registerUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          AuthCookieService cookieService) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.cookieService = cookieService;
    }

    /**
     * Inicia sesión con email y contraseña.
     *
     * <p>Emite las cookies de sesión {@code access_token} y {@code refresh_token}
     * (HttpOnly). El cuerpo de la respuesta no incluye los tokens salvo que se
     * pidan con {@code ?raw=true}; esa variante está pensada para clientes no
     * navegador y exige credenciales válidas.
     *
     * @param request credenciales del usuario
     * @param raw si es {@code true}, devuelve los tokens además de las cookies
     * @return datos de la sesión (o el par de tokens si {@code raw})
     * @throws InvalidCredentialsException si las credenciales no son válidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   @RequestParam(name = "raw", required = false) boolean raw) {
        TokenResponse tokens = loginUseCase.login(request);
        ResponseEntity.BodyBuilder respuesta = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.cookiesDeSesion(tokens).toArray(new String[0]));
        if (raw) {
            return respuesta.body(tokens);
        }
        return respuesta.body(
                new SessionResponse(tokens.tokenType(), tokens.expiresIn(), tokens.refreshExpiresIn()));
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
     * Renueva la sesión a partir del refresh token vigente.
     *
     * <p>Lee la cookie {@code refresh_token} y, si no viene, el token del cuerpo
     * (para clientes sin cookies). Emite un par nuevo de cookies con la rotación
     * correspondiente; los tokens nunca se devuelven en el cuerpo.
     *
     * @param request token de refresco en el cuerpo, opcional si hay cookie
     * @param refreshTokenCookie valor de la cookie {@code refresh_token}
     * @return datos de la sesión renovada
     * @throws InvalidCredentialsException si no llega ningún refresh token
     * @throws TokenRevokedException si el token no es válido, ha expirado o ha sido revocado
     */
    @PostMapping("/refresh")
    public ResponseEntity<SessionResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false)
            String refreshTokenCookie) {
        String refreshToken = refreshTokenCookie != null && !refreshTokenCookie.isBlank()
                ? refreshTokenCookie
                : request != null ? request.refreshToken() : null;
        TokenResponse tokens = refreshTokenUseCase.refrescar(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.cookiesDeSesion(tokens).toArray(new String[0]))
                .body(new SessionResponse(
                        tokens.tokenType(), tokens.expiresIn(), tokens.refreshExpiresIn()));
    }

    /**
     * Cierra la sesión revocando el token de acceso actual y borrando las
     * cookies de sesión del navegador.
     *
     * <p>El token se toma de la cabecera {@code Authorization} o, en su defecto,
     * de la cookie {@code access_token}. Si no llega ninguno, la llamada sigue
     * respondiendo con {@code 204 NO CONTENT} sin tocar nada.
     *
     * @param authorization cabecera {@code Authorization} con el token de acceso
     * @param accessTokenCookie valor de la cookie {@code access_token}
     * @return respuesta vacía con estado {@code 204 NO CONTENT}
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @CookieValue(name = AuthCookieService.ACCESS_COOKIE, required = false)
            String accessTokenCookie) {
        String accessToken = extractBearer(authorization);
        if (accessToken == null) {
            accessToken = accessTokenCookie;
        }
        if (accessToken != null) {
            logoutUseCase.cerrarSesion(accessToken);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.cookiesExpiradas().toArray(new String[0]))
                .build();
    }

    /**
     * Devuelve los datos del usuario autenticado según el token vigente.
     *
     * <p>{@code tenantId} es {@code null} para usuarios de plataforma que no
     * pertenecen a ningún tenant; por eso el mapa se construye con
     * {@link LinkedHashMap} y no con {@code Map.of}, que prohíbe valores nulos.
     *
     * @return mapa con email, usuarioId, tenantId y rol
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        var principal = SecurityContextAuth.principalActual();
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("email", principal.email());
        cuerpo.put("usuarioId", principal.usuarioId());
        cuerpo.put("tenantId", principal.tenantId());
        cuerpo.put("rol", principal.rol());
        return ResponseEntity.ok(cuerpo);
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