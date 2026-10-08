package com.saas.report.usuarios.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record UsuariosPageResponse(
        List<UsuarioResponse> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {

    public static UsuariosPageResponse from(Page<UsuarioResponse> page) {
        return new UsuariosPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }
}