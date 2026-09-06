package com.saas.report.domain.entities;

import com.saas.report.domain.enums.TenantEstado;
import com.saas.report.domain.enums.TipoConexion;
import com.saas.report.domain.exceptions.InvalidIntegerValue;
import com.saas.report.domain.valueObjects.TenantDbConnectionUUID;
import com.saas.report.domain.valueObjects.TenantUUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantDbConnectionTest {

    private Tenant tenant() {
        return new Tenant(TenantUUID.generate(), "Empresa", "empresa", TenantEstado.ACTIVE);
    }

    private TenantDbConnection conexionExterna() {
        return new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant(), "db.example.com", 5432,
                "ventas", "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, null);
    }

    @Test
    void construyeConexionYExponeDatos() {
        Tenant tenant = tenant();
        TenantDbConnectionUUID id = TenantDbConnectionUUID.generate();
        TenantDbConnection conexion = new TenantDbConnection(id, tenant, "db.example.com", 5432,
                "ventas", "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, "public");

        assertThat(conexion.getDbConnectionId()).isEqualTo(id);
        assertThat(conexion.getTenant()).isEqualTo(tenant);
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
        Tenant tenant = tenant();

        assertThatThrownBy(() -> new TenantDbConnection(null, tenant, "host", 5432, "db", "u", "p",
                TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), null, "host", 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, null, 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "  ", 5432,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", null,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", 0,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", 65536,
                "db", "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", 5432,
                null, "u", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", 5432,
                "db", "  ", "p", TipoConexion.EXTERNAL_DB, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant, "host", 5432,
                "db", "u", "p", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detectaConexionExterna() {
        assertThat(conexionExterna().esConexionExterna()).isTrue();

        TenantDbConnection interna = new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant(),
                "localhost", 5432, "report", "app", "p", TipoConexion.INTERNAL_SCHEMA, "esquema");
        assertThat(interna.esConexionExterna()).isFalse();
    }

    @Test
    void construyeElStringDeConexion() {
        TenantDbConnection conexion = new TenantDbConnection(TenantDbConnectionUUID.generate(), tenant(),
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