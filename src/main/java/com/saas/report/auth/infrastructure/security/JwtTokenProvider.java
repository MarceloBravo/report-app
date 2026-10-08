package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.application.TokenService;
import com.saas.report.auth.domain.usuario.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenService {

    private final SecretKey key;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-expiration}") long accessExpirationMillis,
                            @Value("${jwt.refresh-token-expiration}") long refreshExpirationMillis) {
        this.key = buildKey(secret);
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    private SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret no está configurado");
        }
        byte[] keyBytes = decodeKey(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe contener al menos 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeKey(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public String generateAccessToken(AuthUser usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessExpirationMillis);
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.email())
                .claim("userId", usuario.id().toString())
                .claim("rol", usuario.rol())
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key);
        if (usuario.tenantId() != null) {
            builder.claim("tenantId", usuario.tenantId().toString());
        }
        return builder.compact();
    }

    @Override
    public String generateRefreshToken(AuthUser usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpirationMillis);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.email())
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean esTokenValido(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean esRefreshToken(String token) {
        try {
            String typ = claims(token).get("typ", String.class);
            return "refresh".equals(typ);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String extraerEmail(String token) {
        return claims(token).getSubject();
    }

    @Override
    public String extraerUsuarioId(String token) {
        return claims(token).get("userId", String.class);
    }

    @Override
    public String extraerTenantId(String token) {
        return claims(token).get("tenantId", String.class);
    }

    @Override
    public String extraerRol(String token) {
        return claims(token).get("rol", String.class);
    }

    @Override
    public LocalDateTime extraerExpiracion(String token) {
        Instant instant = claims(token).getExpiration().toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime extraerFechaCreacion(String token) {
        Instant instant = claims(token).getIssuedAt().toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims claims(String token) {
        return parseClaims(token);
    }
}