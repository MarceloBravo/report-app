package com.saas.report.auth.infrastructure.persistence.mapper;

import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaEntity;

public final class UsuarioTokenMapper {

    private UsuarioTokenMapper() {
    }

    public static UsuarioTokenJpaEntity toJpa(UsuarioToken token) {
        UsuarioTokenJpaEntity entity = new UsuarioTokenJpaEntity();
        entity.setTokenId(token.getTokenId().getValue());
        AuthUser usuario = token.getUsuario();
        entity.setUsuarioId(usuario.id());
        entity.setUsuarioEmail(usuario.email());
        entity.setUsuarioNombre(usuario.nombre());
        entity.setUsuarioRol(usuario.rol());
        entity.setUsuarioTenantId(usuario.tenantId());
        entity.setAccessToken(token.getAccessToken());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setFechaCreacion(token.getFechaCreacion());
        entity.setAccessTokenExpiraEn(token.getAccessTokenExpiraEn());
        entity.setRefreshTokenExpiraEn(token.getRefreshTokenExpiraEn());
        entity.setActivo(token.estaActivo());
        return entity;
    }

    public static UsuarioToken toDomain(UsuarioTokenJpaEntity entity) {
        AuthUser usuario = new AuthUser(
                entity.getUsuarioId(),
                entity.getUsuarioEmail(),
                entity.getUsuarioNombre(),
                entity.getUsuarioRol(),
                entity.getUsuarioTenantId());
        return new UsuarioToken(new UsuarioTokenUUID(entity.getTokenId()),
                usuario,
                entity.getAccessToken(), entity.getRefreshToken(),
                entity.getFechaCreacion(), entity.getAccessTokenExpiraEn(),
                entity.getRefreshTokenExpiraEn(), entity.getActivo());
    }
}