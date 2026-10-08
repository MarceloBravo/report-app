package com.saas.report.usuarios.infrastructure.persistence.mapper;

import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaEntity;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioJpaEntity toJpa(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setUsuarioId(usuario.getUsuarioId().getValue());
        if (usuario.getTenantId() != null) {
            entity.setTenantId(usuario.getTenantId().getValue());
        } else {
            entity.setTenantId(null);
        }
        entity.setNombre(usuario.getNombre());
        entity.setEmail(usuario.getEmail());
        entity.setPasswordHash(usuario.getPassword());
        entity.setRol(usuario.getRol());
        entity.setActivo(usuario.isActivo());
        entity.setEmailVerificado(usuario.isEmailVerificado());
        return entity;
    }

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        TenantUUID tenantId = entity.getTenantId() != null
                ? new TenantUUID(entity.getTenantId())
                : null;
        return new Usuario(
                new UsuarioUUID(entity.getUsuarioId()),
                tenantId,
                entity.getNombre(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRol(),
                entity.getActivo(),
                entity.getEmailVerificado());
    }
}