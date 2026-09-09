package com.saas.report.auth.domain.token;

import com.saas.report.auth.domain.valueobject.UsuarioTokenUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTokenTest {

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.VIEWER, true);
    }

    private UsuarioToken token(boolean activo, String accessToken, LocalDateTime fechaCreacion,
                               LocalDateTime accessExp, LocalDateTime refreshExp) {
        return new UsuarioToken(UsuarioTokenUUID.generate(), usuario(),
                accessToken, "refresh-token", fechaCreacion, accessExp, refreshExp, activo);
    }

    @Test
    void construyeTokenYExponeDatos() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExp = now.plusMinutes(15);
        LocalDateTime refreshExp = now.plusDays(7);

        UsuarioToken token = new UsuarioToken(UsuarioTokenUUID.generate(), usuario(),
                "access-token", "refresh-token", now, accessExp, refreshExp, true);

        assertThat(token.getAccessToken()).isEqualTo("access-token");
        assertThat(token.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(token.getFechaCreacion()).isEqualTo(now);
        assertThat(token.getAccessTokenExpiraEn()).isEqualTo(accessExp);
        assertThat(token.getRefreshTokenExpiraEn()).isEqualTo(refreshExp);
        assertThat(token.estaActivo()).isTrue();
    }

    @Test
    void rechazaDatosInvalidos() {
        Usuario u = usuario();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExp = now.plusMinutes(15);
        LocalDateTime refreshExp = now.plusDays(7);

        assertThatThrownBy(() -> new UsuarioToken(null, u, "a", "r", now, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), null, "a", "r", now, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), u, null, "r", now, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), u, "  ", "r", now, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), u, "a", null, now, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), u, "a", "r", null, accessExp, refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UsuarioToken(UsuarioTokenUUID.generate(), u, "a", "r", now, now.minusHours(1), refreshExp, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expiraAccessTokenCuandoSuperaSuLimite() {
        LocalDateTime now = LocalDateTime.now();
        UsuarioToken token = token(true, "a", now.minusHours(1), now.minusMinutes(30), now.plusDays(7));

        assertThat(token.estaExpiradoAccessToken()).isTrue();
        assertThat(token.estaExpiradoRefreshToken()).isFalse();
    }

    @Test
    void esValidoSoloSiActivoYRefreshNoExpirado() {
        LocalDateTime now = LocalDateTime.now();

        UsuarioToken activo = token(true, "a", now.minusMinutes(1), now.plusMinutes(14), now.plusDays(7));
        assertThat(activo.esValido()).isTrue();

        UsuarioToken inactivo = token(false, "a", now.minusMinutes(1), now.plusMinutes(14), now.plusDays(7));
        assertThat(inactivo.esValido()).isFalse();

        UsuarioToken refreshVencido = token(true, "a", now.minusDays(8), now.minusDays(7), now.minusDays(1));
        assertThat(refreshVencido.esValido()).isFalse();
    }

    @Test
    void revocaElToken() {
        LocalDateTime now = LocalDateTime.now();
        UsuarioToken token = token(true, "a", now.minusMinutes(1), now.plusMinutes(14), now.plusDays(7));

        token.revocar();

        assertThat(token.estaActivo()).isFalse();
        assertThat(token.esValido()).isFalse();
    }
}