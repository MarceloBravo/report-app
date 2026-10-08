package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.identity.domain.user.UsuarioRol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "c2VjcmV0b19zdXBlcl9zZWd1cm9fZGVfcmVwb3J0X3Nhc18yMDI2X2NsYXZlXzMyYnl0ZQ==";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 900000, 604800000);
    }

    private AuthUser usuario() {
        return new AuthUser(UUID.randomUUID(), "ana@empresa.com", "Ana",
                UsuarioRol.ADMIN.name(), UUID.randomUUID());
    }

    @Test
    void generaAccessTokenConClaimsEsperados() {
        AuthUser u = usuario();

        String token = provider.generateAccessToken(u);

        assertThat(provider.esTokenValido(token)).isTrue();
        assertThat(provider.esRefreshToken(token)).isFalse();
        assertThat(provider.extraerEmail(token)).isEqualTo(u.email());
        assertThat(provider.extraerUsuarioId(token)).isEqualTo(u.id().toString());
        assertThat(provider.extraerTenantId(token)).isEqualTo(u.tenantId().toString());
        assertThat(provider.extraerRol(token)).isEqualTo(UsuarioRol.ADMIN.name());
    }

    @Test
    void generaAccessTokenParaUsuarioPlataformaSinTenant() {
        AuthUser u = new AuthUser(UUID.randomUUID(), "pam@saas.com", "Pam",
                UsuarioRol.PLATFORM_ADMIN.name(), null);

        String token = provider.generateAccessToken(u);

        assertThat(provider.esTokenValido(token)).isTrue();
        assertThat(provider.extraerTenantId(token)).isNull();
        assertThat(provider.extraerRol(token)).isEqualTo(UsuarioRol.PLATFORM_ADMIN.name());
    }

    @Test
    void generaRefreshTokenIdentificable() {
        AuthUser u = usuario();

        String token = provider.generateRefreshToken(u);

        assertThat(provider.esTokenValido(token)).isTrue();
        assertThat(provider.esRefreshToken(token)).isTrue();
        assertThat(provider.extraerEmail(token)).isEqualTo(u.email());
    }

    @Test
    void rechazaTokenInvalido() {
        assertThat(provider.esTokenValido("token-malformado")).isFalse();
        assertThat(provider.esTokenValido("")).isFalse();
        assertThat(provider.esTokenValido(null)).isFalse();
    }

    @Test
    void expiraAccessTokenSegunDuracionConfigurada() {
        AuthUser u = usuario();

        String token = provider.generateAccessToken(u);

        LocalDateTime expiry = provider.extraerExpiracion(token);
        LocalDateTime issued = provider.extraerFechaCreacion(token);

        assertThat(java.time.Duration.between(issued, expiry).toMillis()).isEqualTo(900000);
    }
}