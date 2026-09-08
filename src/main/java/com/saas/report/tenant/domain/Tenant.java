package com.saas.report.tenant.domain;

import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.time.LocalDateTime;

public class Tenant {

    private final TenantUUID tenantId;
    private final String nombreEmpresa;
    private final String subdominioSlug;
    private TenantEstado estado;
    private final LocalDateTime fechaRegistro;

    public Tenant(TenantUUID tenantId, String nombreEmpresa, String subdominioSlug, TenantEstado estado) {
        this(tenantId, nombreEmpresa, subdominioSlug, estado, LocalDateTime.now());
    }

public Tenant(TenantUUID tenantId, String nombreEmpresa, String subdominioSlug, TenantEstado estado,
              LocalDateTime fechaRegistro) {
        if (tenantId == null) {
            throw new IllegalArgumentException("El ID del tenant no puede ser nulo");
        }
        if (nombreEmpresa == null || nombreEmpresa.isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa no puede estar vacío");
        }
        if (subdominioSlug == null || subdominioSlug.isBlank()) {
            throw new IllegalArgumentException("El subdominio no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro no puede ser nula");
        }
        this.tenantId = tenantId;
        this.nombreEmpresa = nombreEmpresa;
        this.subdominioSlug = subdominioSlug;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public TenantUUID getTenantId() {
        return tenantId;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public String getSubdominioSlug() {
        return subdominioSlug;
    }

    public TenantEstado getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean estaActivo() {
        return this.estado == TenantEstado.ACTIVE;
    }

    public void suspender() {
        if (this.estado == TenantEstado.SUSPENDED) {
            throw new InvalidStatusException("El tenant ya está suspendido.");
        }
        this.estado = TenantEstado.SUSPENDED;
    }

    public void reactivar() {
        if (this.estado == TenantEstado.ACTIVE) {
            throw new InvalidStatusException("El tenant ya está activo.");
        }
        this.estado = TenantEstado.ACTIVE;
    }

    public void cancelar() {
        if (this.estado == TenantEstado.CANCELED) {
            throw new InvalidStatusException("El tenant ya está cancelado.");
        }
        this.estado = TenantEstado.CANCELED;
    }

}