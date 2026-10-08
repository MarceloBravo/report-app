package com.saas.report.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad persistida del token de acceso/refresh.
 *
 * <p>Los datos del usuario (email, nombre, rol y tenant) se copian en esta tabla como
 * <em>snapshot</em> al momento de emitir el token, en lugar de asociarla a {@code usuarios}
 * mediante una FK. El motivo es aislar el esquema del BC de auth del BC de usuarios: la tabla
 * existe y se consulta sin depender de la fila de {@code usuarios} ni de un JOIN en la
 * validación de cada petición.
 *
 * <p>Contrapartida de esa duplicidad: la copia es de solo lectura y su vigencia queda ligada a
 * la del token. Los cambios posteriores en {@code usuarios} (rol, email, baja del usuario) no se
 * reflejan en tokens ya emitidos hasta que expiren.
 */
@Entity
@Table(name = "usuario_token")
public class UsuarioTokenJpaEntity {

    @Id
    @Column(name = "token_id", columnDefinition = "uuid")
    private UUID tokenId;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    // Email copiado de 'usuarios' al emitir el token (snapshot, no FK). Permite identificar al
    // titular del token sin consultar la tabla de usuarios.
    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @Column(name = "usuario_nombre")
    private String usuarioNombre;

    // Rol copiado de 'usuarios' al emitir el token (snapshot, no FK). Es el rol con el que se
    // autorizan los requests mientras el token siga vigente: si el rol cambia en 'usuarios',
    // este registro conserva el valor anterior hasta que el token expire.
    @Column(name = "usuario_rol", nullable = false)
    private String usuarioRol;

    @Column(name = "usuario_tenant_id", columnDefinition = "uuid")
    private UUID usuarioTenantId;

    @Column(name = "access_token", nullable = false, length = 4096)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 4096)
    private String refreshToken;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "access_token_expira_en", nullable = false)
    private LocalDateTime accessTokenExpiraEn;

    @Column(name = "refresh_token_expira_en", nullable = false)
    private LocalDateTime refreshTokenExpiraEn;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioRol() {
        return usuarioRol;
    }

    public void setUsuarioRol(String usuarioRol) {
        this.usuarioRol = usuarioRol;
    }

    public UUID getUsuarioTenantId() {
        return usuarioTenantId;
    }

    public void setUsuarioTenantId(UUID usuarioTenantId) {
        this.usuarioTenantId = usuarioTenantId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getAccessTokenExpiraEn() {
        return accessTokenExpiraEn;
    }

    public void setAccessTokenExpiraEn(LocalDateTime accessTokenExpiraEn) {
        this.accessTokenExpiraEn = accessTokenExpiraEn;
    }

    public LocalDateTime getRefreshTokenExpiraEn() {
        return refreshTokenExpiraEn;
    }

    public void setRefreshTokenExpiraEn(LocalDateTime refreshTokenExpiraEn) {
        this.refreshTokenExpiraEn = refreshTokenExpiraEn;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}