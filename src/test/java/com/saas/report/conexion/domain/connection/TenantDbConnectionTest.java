package com.saas.report.conexion.domain.connection;

import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantDbConnectionTest {

    private TenantUUID tenantUuid() {
        return TenantUUID.generate();
    }

    private TenantDbConnection conexionExterna() {
        return new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantUuid(), "db.example.com", 5432,
                "ventas", "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, null);
    }

    @Test
    void construyeConexionYExponeDatos() {
        TenantUUID tenantId = tenantUuid();
        TenantDbConnectionUUID id = TenantDbConnectionUUID.generate();
        TenantDbConnection conexion = new TenantDbConnection(id, tenantId, "db.example.com", 5432,
                "ventas", "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, "public");

        assertThat(conexion.getDbConnectionId()).isEqualTo(id);
        assertThat(conexion.getTenantId()).isEqualTo(tenantId);
        assertThat(conexion.getHost()).isEqualTo("db.example.com");
        assertThat(conexion.getPuerto()).isEqualTo(5432);
        assertThat(conexion.getDbName()).isEqualTo("ventas");
        assertThat(conexion.getDbUser()).isEqualTo("report_app");
        assertThat(conexion.getDbPasswordEncrypted()).isEqualTo("enc:clave");
        assertThat(conexion.getTipoConexion()).isEqualTo(TipoConexion.EXTERNAL_DB);
        assertThat(conexion.getSchemaName()).isEqualTo("public");
    }

    @Test
    void rechazaDatosInvalidos() {
        TenantUUID tenantId = tenantUuid();

        assertThatThrownBy(() -> new TenantDbConnection(null, tenantId, "host", 5432, "db", "u", "p",
                TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), null, "host", 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, null, 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "  ", 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", null,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", 0,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", 65536,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", 5432,
                null, "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", 5432,
                "db", "  ", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantId, "host", 5432,
                "db", "u", "p", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detectaConexionExterna() {
        assertThat(conexionExterna().esConexionExterna()).isTrue();

        TenantDbConnection interna = new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantUuid(),
                "localhost", 5432, "report", "app", "p", TipoConexion.INTERNAL_SCHEMA, "esquema");
        assertThat(interna.esConexionExterna()).isFalse();
    }

    @Test
    void construyeElStringDeConexion() {
        TenantDbConnection conexion = new TenantDbConnection(TenantDbConnectionUUID.generate(), tenantUuid(),
                "db.example.com", 5432, "ventas", "u", "p", TipoConexion.EXTERNAL_DB, null);

        assertThat(conexion.getConnectionString()).isEqualTo("jdbc:postgresql://db.example.com:5432/ventas");
    }

    @Test
    void actualizaCredenciales() {
        TenantDbConnection conexion = conexionExterna();
        conexion.actualizarCredenciales("nuevo_usuario", "enc:nueva_clave");

        assertThat(conexion.getDbUser()).isEqualTo("nuevo_usuario");
        assertThat(conexion.getDbPasswordEncrypted()).isEqualTo("enc:nueva_clave");

        assertThatThrownBy(() -> conexion.actualizarCredenciales("  ", "p"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> conexion.actualizarCredenciales("u", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void marcarSoloLecturaCambiaElTipoDeConexion() {
        TenantDbConnection conexion = conexionExterna();
        conexion.marcarSoloLectura();

        assertThat(conexion.getTipoConexion()).isEqualTo(TipoConexion.EXTERNAL_DB);
    }
}