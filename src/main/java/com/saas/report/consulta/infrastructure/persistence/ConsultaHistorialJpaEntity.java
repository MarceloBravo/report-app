package com.saas.report.consulta.infrastructure.persistence;

import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consulta_historial", indexes = {
            @Index(name = "idx_consulta_historial_tenant_fecha",
                    columnList = "tenant_id, fecha_consulta")
    })
@Getter
@Setter
@NoArgsConstructor
public class ConsultaHistorialJpaEntity {

    @Id
    @Column(name = "consulta_id", columnDefinition = "uuid")
    private UUID consultaId;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "pregunta_usuario", nullable = false)
    private String preguntaUsuario;

    @Column(name = "query_sql_generada", nullable = false, columnDefinition = "text")
    private String querySqlGenerada;

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(name = "exitosa")
    private Boolean exitosa;

    @Column(name = "registros_count")
    private Integer registrosCount;

    @Column(name = "db_connection_id", columnDefinition = "uuid")
    private UUID dbConnectionId;

    @Column(name = "consultas_en_periodo", nullable = false)
    private Integer consultasEnPeriodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motor_bd")
    private MotorBaseDatos motorBd;
}