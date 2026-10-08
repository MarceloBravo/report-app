package com.saas.report.usuarios.infrastructure.security;

import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Component
public class HmacEmailVerificationTokenService implements EmailVerificationTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int HASH_BITS = 256;

    private final byte[] key;
    private final long expirationMillis;

    public HmacEmailVerificationTokenService(
            @Value("${app.encryption.key}") String base64Key,
            @Value("${app.onboarding.email-verification.expiration-millis:86400000}") long expirationMillis) {
        this.key = derivarClave(base64Key);
        this.expirationMillis = expirationMillis;
    }

    @Override
    public String generar(UsuarioUUID usuarioId, String email) {
        String payload = usuarioId.getValue() + ":" + email.toLowerCase(Locale.ROOT);
        long expiracion = System.currentTimeMillis() + expirationMillis;
        String mac = calcularMac(payload + "." + expiracion);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + expiracion + "." + mac;
    }

    @Override
    public UsuarioUUID verificar(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de verificación inválido");
        }
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new IllegalArgumentException("Token de verificación inválido");
        }
        long expiracion;
        String payload;
        try {
            expiracion = Long.parseLong(partes[1]);
            payload = new String(
                    Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Token de verificación inválido", e);
        }
        if (expiracion < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Token de verificación expirado");
        }
        String macEsperada = calcularMac(payload + "." + partes[1]);
        if (!MessageDigest.isEqual(
                macEsperada.getBytes(StandardCharsets.UTF_8),
                partes[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Token de verificación inválido");
        }
        try {
            return new UsuarioUUID(java.util.UUID.fromString(payload.split(":")[0]));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Token de verificación inválido", e);
        }
    }

    private String calcularMac(String contenido) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el token de verificación", e);
        }
    }

    private byte[] derivarClave(String base64Key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(
                    ("email-verification:" + base64Key).getBytes(StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Override
    public Boolean esValido(UUID usuarioId, String token) {
        try {
            UsuarioUUID idExtraido = verificar(token);
            return idExtraido.equals(new UsuarioUUID(usuarioId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public UsuarioUUID extraerUsuarioId(String token) {
        try {
            return verificar(token);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}