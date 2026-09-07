package com.saas.report.tenant.domain.connection;

import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;

public class TenantDbConnection {

    private final TenantDbConnectionUUID dbConnectionId;
    private final TenantUUID tenantId;
    private final String host;
    private final Integer puerto;
    private final String dbName;
    private String dbUser;
    private String dbPasswordEncrypted;
    private TipoConexion tipoConexion;
    private String schemaName;

    public TenantDbConnection(TenantDbConnectionUUID dbConnectionId, TenantUUID tenantId, String host, Integer puerto,
                              String dbName, String dbUser, String dbPasswordEncrypted,
                              TipoConexion tipoConexion, String schemaName) {
        if (dbConnectionId == null) {
            throw new IllegalArgumentException("El ID de la conexión no puede ser nulo");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenant no puede ser nulo");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("El host no puede estar vacío");
        }
        if (puerto == null || puerto < 1 || puerto > 65535) {
            throw new InvalidIntegerValue("El puerto debe estar entre 1 y 65535");
        }
        if (dbName == null || dbName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la base no puede estar vacío");
        }
        if (dbUser == null || dbUser.isBlank()) {
            throw new IllegalArgumentException("El usuario de la base no puede estar vacío");
        }
        if (tipoConexion == null) {
            throw new IllegalArgumentException("El tipo de conexión no puede ser nulo");
        }
        this.dbConnectionId = dbConnectionId;
        this.tenantId = tenantId;
        this.host = host;
        this.puerto = puerto;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPasswordEncrypted = dbPasswordEncrypted;
        this.tipoConexion = tipoConexion;
        this.schemaName = schemaName;
    }

    public TenantDbConnectionUUID getDbConnectionId() {
        return dbConnectionId;
    }

    public TenantUUID getTenantId() {
        return tenantId;
    }

    public String getHost() {
        return host;
    }

    public Integer getPuerto() {
        return puerto;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPasswordEncrypted() {
        return dbPasswordEncrypted;
    }

    public TipoConexion getTipoConexion() {
        return tipoConexion;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public boolean esConexionExterna() {
        return this.tipoConexion == TipoConexion.EXTERNAL_DB;
    }

    public String getConnectionString() {
        return "jdbc:postgresql://" + this.host + ":" + this.puerto + "/" + this.dbName;
    }

    public void actualizarCredenciales(String user, String password) {
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        this.dbUser = user;
        this.dbPasswordEncrypted = password;
    }

    public void marcarSoloLectura() {
        this.tipoConexion = TipoConexion.EXTERNAL_DB;
    }
}