package com.saas.report.auth.presentation;

import com.saas.report.auth.application.AuthPrincipal;
import com.saas.report.auth.application.dto.LoginRequest;
import com.saas.report.auth.application.dto.RefreshTokenRequest;
import com.saas.report.auth.application.dto.RegisterRequest;
import com.saas.report.auth.application.dto.TokenResponse;
import com.saas.report.auth.application.usecase.LoginUseCase;
import com.saas.report.auth.application.usecase.LogoutUseCase;
import com.saas.report.auth.application.usecase.RefreshTokenUseCase;
import com.saas.report.auth.application.usecase.RegisterUseCase;
import com.saas.report.identity.domain.user.UsuarioRol;
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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.registrar(request, UsuarioRol.VIEWER);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/register")
    public ResponseEntity<Void> adminRegister(@Valid @RequestBody RegisterRequest request) {
        UsuarioRol rol = request.rol() != null ? request.rol() : UsuarioRol.VIEWER;
        registerUseCase.registrar(request, rol);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = refreshTokenUseCase.refrescar(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String accessToken = extractBearer(authorization);
        logoutUseCase.cerrarSesion(accessToken);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "email", principal.email(),
                "usuarioId", principal.usuarioId(),
                "tenantId", principal.tenantId(),
                "rol", principal.rol()));
    }

    private String extractBearer(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return null;
    }
}