package com.saas.report.auth.domain.token;

import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;

import java.time.LocalDateTime;

public class UsuarioToken {

    private final UsuarioTokenUUID tokenId;
    private final Usuario usuario;
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime accessTokenExpiraEn;
    private final LocalDateTime refreshTokenExpiraEn;
    private boolean activo;

    public UsuarioToken(UsuarioTokenUUID tokenId, Usuario usuario, String accessToken, String refreshToken,
                        LocalDateTime fechaCreacion, LocalDateTime accessTokenExpiraEn,
                        LocalDateTime refreshTokenExpiraEn, boolean activo) {
        if (tokenId == null) {
            throw new IllegalArgumentException("El ID del token no puede ser nulo");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("El access token no puede estar vacío");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("El refresh token no puede estar vacío");
        }
        if (fechaCreacion == null) {
            throw new IllegalArgumentException("La fecha de creación no puede ser nula");
        }
        if (accessTokenExpiraEn == null || accessTokenExpiraEn.isBefore(fechaCreacion)) {
            throw new IllegalArgumentException("La fecha de expiración del access token no es válida");
        }
        if (refreshTokenExpiraEn == null || refreshTokenExpiraEn.isBefore(accessTokenExpiraEn)) {
            throw new IllegalArgumentException("La fecha de expiración del refresh token no es válida");
        }
        this.tokenId = tokenId;
        this.usuario = usuario;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.fechaCreacion = fechaCreacion;
        this.accessTokenExpiraEn = accessTokenExpiraEn;
        this.refreshTokenExpiraEn = refreshTokenExpiraEn;
        this.activo = activo;
    }

    public UsuarioTokenUUID getTokenId() {
        return tokenId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getAccessTokenExpiraEn() {
        return accessTokenExpiraEn;
    }

    public LocalDateTime getRefreshTokenExpiraEn() {
        return refreshTokenExpiraEn;
    }

    public boolean estaActivo() {
        return this.activo;
    }

    public boolean estaExpiradoAccessToken() {
        return LocalDateTime.now().isAfter(this.accessTokenExpiraEn);
    }

    public boolean estaExpiradoRefreshToken() {
        return LocalDateTime.now().isAfter(this.refreshTokenExpiraEn);
    }

    public boolean esValido() {
        return this.activo && !estaExpiradoRefreshToken();
    }

    public void revocar() {
        this.activo = false;
    }
}