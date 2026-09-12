package com.saas.report.usuarios.infrastructure.persistence.mapper;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaEntity;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
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
        entity.setEmailVerificado(usuario.getEmailVerificado());
        return entity;
    }

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        TenantUUID tenantId = entity.getTenant() != null
                ? TenantMapper.toDomain(entity.getTenant()).getTenantId()
                : null;
        return new Usuario(new UsuarioUUID(entity.getUsuarioId()),
                tenantId,
                entity.getNombre(), entity.getEmail(), entity.getPasswordHash(),
                entity.getRol(), entity.getActivo(), entity.getEmailVerificado());
    }
}