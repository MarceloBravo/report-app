package com.saas.report.consulta.domain;

import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

import java.time.LocalDateTime;

public class ConsultaHistorial {

    private final ConsultaHistorialUUID consultaId;
    private final TenantUUID tenantId;
    private final UsuarioUUID usuarioId;
    private final String preguntaUsuario;
    private final String querySqlGenerada;
    private final LocalDateTime fechaConsulta;
    private final Boolean exitosa;
    private final Integer registrosCount;
    private final TenantDbConnectionUUID dbConnectionId;
    private final Integer consultasEnPeriodo;

    public ConsultaHistorial(ConsultaHistorialUUID consultaId, TenantUUID tenantId, UsuarioUUID usuarioId,
                             String preguntaUsuario, String querySqlGenerada, LocalDateTime fechaConsulta,
                             Boolean exitosa, Integer registrosCount, TenantDbConnectionUUID dbConnectionId,
                             Integer consultasEnPeriodo) {
        if (consultaId == null) {
            throw new IllegalArgumentException("El ID de la consulta no puede ser nulo");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (preguntaUsuario == null || preguntaUsuario.isBlank()) {
            throw new IllegalArgumentException("La pregunta del usuario no puede estar vacía");
        }
        if (fechaConsulta == null) {
            throw new IllegalArgumentException("La fecha de consulta no puede ser nula");
        }
        if (consultasEnPeriodo == null || consultasEnPeriodo < 1) {
            throw new InvalidIntegerValue("Las consultas del período deben ser >= 1 (incluyen la consulta actual)");
        }
        this.consultaId = consultaId;
        this.tenantId = tenantId;
        this.usuarioId = usuarioId;
        this.preguntaUsuario = preguntaUsuario;
        this.querySqlGenerada = querySqlGenerada;
        this.fechaConsulta = fechaConsulta;
        this.exitosa = exitosa;
        this.registrosCount = registrosCount;
        this.dbConnectionId = dbConnectionId;
        this.consultasEnPeriodo = consultasEnPeriodo;
    }

    public ConsultaHistorialUUID getConsultaId() {
        return consultaId;
    }

    public TenantUUID getTenantId() {
        return tenantId;
    }

    public UsuarioUUID getUsuarioId() {
        return usuarioId;
    }

    public String getPreguntaUsuario() {
        return preguntaUsuario;
    }

    public String getQuerySqlGenerada() {
        return querySqlGenerada;
    }

    public LocalDateTime getFechaConsulta() {
        return fechaConsulta;
    }

    public Boolean getExitosa() {
        return exitosa;
    }

    public Integer getRegistrosCount() {
        return registrosCount;
    }

    public TenantDbConnectionUUID getDbConnectionId() {
        return dbConnectionId;
    }

    public Integer getConsultasEnPeriodo() {
        return consultasEnPeriodo;
    }

    public boolean fueExitosa() {
        return Boolean.TRUE.equals(exitosa);
    }

    public String getResumen() {
        if (preguntaUsuario == null) {
            return "";
        }
        return preguntaUsuario.length() <= 100
                ? preguntaUsuario
                : preguntaUsuario.substring(0, 100);
    }

    public boolean superaLimite(Integer limiteConsultas) {
        if (limiteConsultas == null || limiteConsultas < 0) {
            return false;
        }
        return this.consultasEnPeriodo > limiteConsultas;
    }
}