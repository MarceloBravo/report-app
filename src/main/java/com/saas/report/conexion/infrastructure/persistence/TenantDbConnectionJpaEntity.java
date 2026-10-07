package com.saas.report.conexion.infrastructure.persistence;

import com.saas.report.conexion.domain.connection.TipoConexion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tenant_db_connection")
@Getter
@Setter
@NoArgsConstructor
public class TenantDbConnectionJpaEntity {

    @Id
    @Column(name = "db_connection_id", columnDefinition = "uuid")
    private UUID dbConnectionId;

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "puerto", nullable = false)
    private Integer puerto;

    @Column(name = "db_name", nullable = false)
    private String dbName;

    @Column(name = "db_user", nullable = false)
    private String dbUser;

    @Column(name = "db_password_encrypted", nullable = false, columnDefinition = "text")
    private String dbPasswordEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conexion", nullable = false)
    private TipoConexion tipoConexion;

    @Column(name = "schema_name")
    private String schemaName;
}