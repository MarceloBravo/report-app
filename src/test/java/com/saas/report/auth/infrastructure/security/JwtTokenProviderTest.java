package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "c2VjcmV0b19zdXBlcl9zZWd1cm9fZGVfcmVwb3J0X3Nhc18yMDI2X2NsYXZlXzMyYnl0ZQ==";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 900000, 604800000);
    }

    private Usuario usuario() {
        return new Usuario(UsuarioUUID.generate(), TenantUUID.generate(), "Ana", "ana@empresa.com",
                "hash", UsuarioRol.ADMIN, true);
    }

    @Test
    void generaAccessTokenConClaimsEsperados() {
        Usuario u = usuario();

        String token = provider.generateAccessToken(u);

        assertThat(provider.esTokenValido(token)).isTrue();
        assertThat(provider.esRefreshToken(token)).isFalse();
        assertThat(provider.extraerEmail(token)).isEqualTo(u.getEmail());
        assertThat(provider.extraerUsuarioId(token)).isEqualTo(u.getUsuarioId().getValue().toString());
        assertThat(provider.extraerTenantId(token)).isEqualTo(u.getTenantId().getValue().toString());
        assertThat(provider.extraerRol(token)).isEqualTo(UsuarioRol.ADMIN.name());
    }

    @Test
    void generaRefreshTokenIdentificable() {
        Usuario u = usuario();

        String token = provider.generateRefreshToken(u);

        assertThat(provider.esTokenValido(token)).isTrue();
        assertThat(provider.esRefreshToken(token)).isTrue();
        assertThat(provider.extraerEmail(token)).isEqualTo(u.getEmail());
    }

    @Test
    void rechazaTokenInvalido() {
        assertThat(provider.esTokenValido("token-malformado")).isFalse();
        assertThat(provider.esTokenValido("")).isFalse();
        assertThat(provider.esTokenValido(null)).isFalse();
    }

    @Test
    void expiraAccessTokenSegunDuracionConfigurada() {
        Usuario u = usuario();

        String token = provider.generateAccessToken(u);

        LocalDateTime expiry = provider.extraerExpiracion(token);
        LocalDateTime issued = provider.extraerFechaCreacion(token);

        assertThat(java.time.Duration.between(issued, expiry).toMillis()).isEqualTo(900000);
    }
}