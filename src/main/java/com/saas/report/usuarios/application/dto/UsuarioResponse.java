package com.saas.report.usuarios.application.dto;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.identity.domain.user.UsuarioRol;

import java.util.UUID;

public record UsuarioResponse(
        UUID usuarioId,
        UUID tenantId,
        String nombre,
        String email,
        UsuarioRol rol,
        boolean activo
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getUsuarioId().getValue(),
                usuario.getTenantId().getValue(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo());
    }
}