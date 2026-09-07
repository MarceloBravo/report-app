package com.saas.report.auth.application;

import com.saas.report.auth.domain.exception.EmailAlreadyExistsException;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.exception.TokenRevokedException;
import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.user.UsuarioRepository;
import com.saas.report.auth.domain.user.UsuarioRol;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.auth.presentation.dto.RegisterRequest;
import com.saas.report.auth.presentation.dto.TokenResponse;
import com.saas.report.domain.entities.Tenant;
import com.saas.report.domain.exceptions.InvalidStatusException;
import com.saas.report.domain.exceptions.ResourceNotFoundException;
import com.saas.report.domain.repositories.TenantRepository;
import com.saas.report.domain.valueObjects.TenantUUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTokenRepository usuarioTokenRepository;
    private final TenantRepository tenantRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       UsuarioTokenRepository usuarioTokenRepository,
                       TenantRepository tenantRepository,
                       TokenService tokenService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.usuarioTokenRepository = usuarioTokenRepository;
        this.tenantRepository = tenantRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TokenResponse login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        return emitirTokens(usuario);
    }

    @Transactional
    public void registrarUsuarioPublico(RegisterRequest request) {
        registrar(request, UsuarioRol.VIEWER);
    }

    @Transactional
    public void registrarUsuarioAdmin(RegisterRequest request) {
        UsuarioRol rol = request.rol() != null ? request.rol() : UsuarioRol.VIEWER;
        registrar(request, rol);
    }

    @Transactional
    public TokenResponse refrescarToken(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            throw new InvalidCredentialsException("El refresh token es requerido");
        }
        if (!tokenService.esRefreshToken(refreshTokenString)) {
            throw new TokenRevokedException("El refresh token no es válido");
        }

        UsuarioToken registro = usuarioTokenRepository.findByRefreshToken(refreshTokenString)
                .orElseThrow(() -> new TokenRevokedException("El refresh token ha sido revocado"));

        if (!registro.estaActivo()) {
            throw new TokenRevokedException("El refresh token ha sido revocado");
        }
        if (registro.estaExpiradoRefreshToken()) {
            registro.revocar();
            usuarioTokenRepository.save(registro);
            throw new TokenRevokedException("El refresh token ha expirado");
        }

        Usuario usuario = registro.getUsuario();
        registro.revocar();
        usuarioTokenRepository.save(registro);

        return emitirTokens(usuario);
    }

    @Transactional
    public void cerrarSesion(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new InvalidCredentialsException("El token es requerido");
        }
        usuarioTokenRepository.findByAccessToken(accessToken)
                .ifPresent(registro -> {
                    registro.revocar();
                    usuarioTokenRepository.save(registro);
                });
        SecurityContextHolder.clearContext();
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        String accessToken = tokenService.generateAccessToken(usuario);
        String refreshToken = tokenService.generateRefreshToken(usuario);

        LocalDateTime fechaCreacion = tokenService.extraerFechaCreacion(accessToken);
        LocalDateTime fechaExpiracionAccess = tokenService.extraerExpiracion(accessToken);
        LocalDateTime fechaExpiracionRefresh = tokenService.extraerExpiracion(refreshToken);

        UsuarioToken registro = new UsuarioToken(
                UsuarioTokenUUID.generate(),
                usuario,
                accessToken,
                refreshToken,
                fechaCreacion,
                fechaExpiracionAccess,
                fechaExpiracionRefresh,
                true);
        usuarioTokenRepository.save(registro);

        long expiresInAccess = Duration.between(fechaCreacion, fechaExpiracionAccess).toMillis();
        long expiresInRefresh = Duration.between(fechaCreacion, fechaExpiracionRefresh).toMillis();

        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInAccess, expiresInRefresh);
    }

    private void registrar(RegisterRequest request, UsuarioRol rol) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email '" + request.email() + "' ya está registrado");
        }

        Tenant tenant = tenantRepository.findById(new TenantUUID(request.tenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con ID: " + request.tenantId()));

        if (!tenant.estaActivo()) {
            throw new InvalidStatusException("El tenant no se encuentra activo");
        }

        Usuario usuario = new Usuario(
                UsuarioUUID.generate(),
                tenant,
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                rol,
                true);

        usuarioRepository.save(usuario);
    }
}