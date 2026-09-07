package com.saas.report.auth.infrastructure.persistence.mapper;

import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.auth.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaEntity;

public final class UsuarioTokenMapper {

    private UsuarioTokenMapper() {
    }

    public static UsuarioTokenJpaEntity toJpa(UsuarioToken token, UsuarioJpaEntity usuarioJpa) {
        UsuarioTokenJpaEntity entity = new UsuarioTokenJpaEntity();
        entity.setTokenId(token.getTokenId().getValue());
        entity.setUsuario(usuarioJpa);
        entity.setAccessToken(token.getAccessToken());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setFechaCreacion(token.getFechaCreacion());
        entity.setAccessTokenExpiraEn(token.getAccessTokenExpiraEn());
        entity.setRefreshTokenExpiraEn(token.getRefreshTokenExpiraEn());
        entity.setActivo(token.estaActivo());
        return entity;
    }

    public static UsuarioToken toDomain(UsuarioTokenJpaEntity entity) {
        return new UsuarioToken(new UsuarioTokenUUID(entity.getTokenId()),
                UsuarioMapper.toDomain(entity.getUsuario()),
                entity.getAccessToken(), entity.getRefreshToken(),
                entity.getFechaCreacion(), entity.getAccessTokenExpiraEn(),
                entity.getRefreshTokenExpiraEn(), entity.getActivo());
    }
}