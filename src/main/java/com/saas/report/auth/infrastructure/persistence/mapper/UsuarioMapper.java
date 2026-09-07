package com.saas.report.auth.infrastructure.persistence.mapper;

import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.auth.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaEntity;
import com.saas.report.tenant.infrastructure.persistence.mapper.TenantMapper;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioJpaEntity toJpa(Usuario usuario, TenantJpaEntity tenantJpa) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setUsuarioId(usuario.getUsuarioId().getValue());
        entity.setTenant(tenantJpa);
        entity.setNombre(usuario.getNombre());
        entity.setEmail(usuario.getEmail());
        entity.setPasswordHash(usuario.getPassword());
        entity.setRol(usuario.getRol());
        entity.setActivo(usuario.getActivo());
        return entity;
    }

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(new UsuarioUUID(entity.getUsuarioId()),
                TenantMapper.toDomain(entity.getTenant()).getTenantId(),
                entity.getNombre(), entity.getEmail(), entity.getPasswordHash(),
                entity.getRol(), entity.getActivo());
    }
}