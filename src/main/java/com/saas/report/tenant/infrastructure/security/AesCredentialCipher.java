package com.saas.report.tenant.infrastructure.security;

import com.saas.report.tenant.domain.connection.CredentialCipher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesCredentialCipher implements CredentialCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;

    public AesCredentialCipher(@Value("${app.encryption.key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "La clave de cifrado (ENCRYPTION_KEY) debe ser de 16, 24 o 32 bytes en Base64");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String cifrar(String valorPlano) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(valorPlano.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cifrado.length);
            buffer.put(iv);
            buffer.put(cifrado);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar la credencial", e);
        }
    }

    @Override
    public String descifrar(String valorCifrado) {
        try {
            byte[] completo = Base64.getDecoder().decode(valorCifrado);
            byte[] iv = Arrays.copyOfRange(completo, 0, IV_LENGTH);
            byte[] cifrado = Arrays.copyOfRange(completo, IV_LENGTH, completo.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo descifrar la credencial", e);
        }
    }
}