package com.saas.report.consulta.domain;

import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.InvalidIntegerValue;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsultaHistorialTest {

    private TenantUUID tenant() {
        return TenantUUID.generate();
    }

    private UsuarioUUID usuario() {
        return UsuarioUUID.generate();
    }

    private ConsultaHistorial consulta(Boolean exitosa, Integer consultasEnPeriodo) {
        return new ConsultaHistorial(ConsultaHistorialUUID.generate(), tenant(), usuario(),
                "¿Cuántas ventas hubo en marzo?", "SELECT * FROM ventas;", LocalDateTime.now(),
                exitosa, 42, TenantDbConnectionUUID.generate(), consultasEnPeriodo);
    }

    @Test
    void construyeConsultaYExponeDatos() {
        TenantUUID tenant = tenant();
        UsuarioUUID usuario = usuario();
        ConsultaHistorialUUID id = ConsultaHistorialUUID.generate();
        LocalDateTime fecha = LocalDateTime.now();
        TenantDbConnectionUUID conexionId = TenantDbConnectionUUID.generate();
        ConsultaHistorial consulta = new ConsultaHistorial(id, tenant, usuario, "¿Cuántas ventas?",
                "SELECT 1;", fecha, true, 10, conexionId, 3);

        assertThat(consulta.getConsultaId()).isEqualTo(id);
        assertThat(consulta.getTenantId()).isEqualTo(tenant);
        assertThat(consulta.getUsuarioId()).isEqualTo(usuario);
        assertThat(consulta.getPreguntaUsuario()).isEqualTo("¿Cuántas ventas?");
        assertThat(consulta.getQuerySqlGenerada()).isEqualTo("SELECT 1;");
        assertThat(consulta.getFechaConsulta()).isEqualTo(fecha);
        assertThat(consulta.getExitosa()).isTrue();
        assertThat(consulta.getRegistrosCount()).isEqualTo(10);
        assertThat(consulta.getDbConnectionId()).isEqualTo(conexionId);
        assertThat(consulta.getConsultasEnPeriodo()).isEqualTo(3);
    }

    @Test
    void rechazaDatosInvalidos() {
        ConsultaHistorialUUID id = ConsultaHistorialUUID.generate();
        TenantUUID tenant = tenant();
        UsuarioUUID usuario = usuario();

        assertThatThrownBy(() -> new ConsultaHistorial(null, tenant, usuario, "p", "q", LocalDateTime.now(),
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsultaHistorial(id, null, usuario, "p", "q", LocalDateTime.now(),
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsultaHistorial(id, tenant, null, "p", "q", LocalDateTime.now(),
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsultaHistorial(id, tenant, usuario, null, "q", LocalDateTime.now(),
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsultaHistorial(id, tenant, usuario, "  ", "q", LocalDateTime.now(),
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsultaHistorial(id, tenant, usuario, "p", "q", null,
                true, 0, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaConsumoDePeriodoInvalido() {
        assertThatThrownBy(() -> new ConsultaHistorial(ConsultaHistorialUUID.generate(), tenant(), usuario(),
                "p", "q", LocalDateTime.now(), true, 0, null, null))
                .isInstanceOf(InvalidIntegerValue.class);
        assertThatThrownBy(() -> new ConsultaHistorial(ConsultaHistorialUUID.generate(), tenant(), usuario(),
                "p", "q", LocalDateTime.now(), true, 0, null, 0))
                .isInstanceOf(InvalidIntegerValue.class);
    }

    @Test
    void fueExitosaSegunElFlag() {
        assertThat(consulta(true, 1).fueExitosa()).isTrue();
        assertThat(consulta(false, 1).fueExitosa()).isFalse();
    }

    @Test
    void devuelveElResumenCompletoCuandoEsCorto() {
        ConsultaHistorial consulta = consulta(true, 1);

        assertThat(consulta.getResumen()).isEqualTo("¿Cuántas ventas hubo en marzo?");
    }

    @Test
    void truncaElResumenA100Caracteres() {
        String preguntaLarga = "¿".repeat(0).concat("a".repeat(150));
        ConsultaHistorial consulta = new ConsultaHistorial(ConsultaHistorialUUID.generate(), tenant(), usuario(),
                preguntaLarga, "q", LocalDateTime.now(), true, 0, null, 1);

        assertThat(consulta.getResumen()).hasSize(100);
        assertThat(consulta.getResumen()).isEqualTo(preguntaLarga.substring(0, 100));
    }

    @Test
    void noSuperaLimiteCuandoElPlanEsIlimitado() {
        ConsultaHistorial consulta = consulta(true, 50);

        assertThat(consulta.superaLimite(null)).isFalse();
        assertThat(consulta.superaLimite(-1)).isFalse();
    }

    @Test
    void superaLimiteComparaElConsumoConElTope() {
        ConsultaHistorial consumoCinco = new ConsultaHistorial(ConsultaHistorialUUID.generate(), tenant(), usuario(),
                "p", "q", LocalDateTime.now(), true, 0, null, 5);

        assertThat(consumoCinco.superaLimite(5)).isFalse();
        assertThat(consumoCinco.superaLimite(4)).isTrue();
    }
}