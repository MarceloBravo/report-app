package com.saas.report.domain.entities;

import com.saas.report.domain.exceptions.InvalidIntegerValue;
import com.saas.report.domain.valueObjects.ConsultaHistorialUUID;
import com.saas.report.domain.valueObjects.TenantDbConnectionUUID;

import java.time.LocalDateTime;

public class ConsultaHistorial {

    private final ConsultaHistorialUUID consultaId;
    private final Tenant tenant;
    private final Usuario usuario;
    private final String preguntaUsuario;
    private final String querySqlGenerada;
    private final LocalDateTime fechaConsulta;
    private final Boolean exitosa;
    private final Integer registrosCount;
    private final TenantDbConnectionUUID dbConnectionId;
    private final Integer consultasEnPeriodo;

    public ConsultaHistorial(ConsultaHistorialUUID consultaId, Tenant tenant, Usuario usuario,
                             String preguntaUsuario, String querySqlGenerada, LocalDateTime fechaConsulta,
                             Boolean exitosa, Integer registrosCount, TenantDbConnectionUUID dbConnectionId,
                             Integer consultasEnPeriodo) {
        if (consultaId == null) {
            throw new IllegalArgumentException("El ID de la consulta no puede ser nulo");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (usuario == null) {
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
        this.tenant = tenant;
        this.usuario = usuario;
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

    public Tenant getTenant() {
        return tenant;
    }

    public Usuario getUsuario() {
        return usuario;
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