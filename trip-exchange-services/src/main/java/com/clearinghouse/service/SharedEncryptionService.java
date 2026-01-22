package com.clearinghouse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SharedEncryptionService {

    @Value("${security.login-validate.sharedKey:}")
    private String sharedKeyBase64;

    @Value("${security.login-validate.ttlSeconds:120}")
    private long ttlSeconds;

    // Decrypts an encoded string into plaintext. Expected format when sharedKey is present:
    // Base64( IV (12 bytes) || ciphertext || tag )
    // If sharedKey is not configured, treats input as Base64-encoded plaintext and returns the decoded string.
    public String decryptToString(String encoded) throws IllegalArgumentException {
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalArgumentException("encoded value is empty");
        }

        try {
            if (sharedKeyBase64 == null || sharedKeyBase64.isBlank()) {
                // Fallback: treat input as base64-encoded plaintext
                byte[] decoded = Base64.getDecoder().decode(encoded);
                return new String(decoded, StandardCharsets.UTF_8);
            }

            byte[] keyBytes = Base64.getDecoder().decode(sharedKeyBase64);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] all = Base64.getDecoder().decode(encoded);
            if (all.length < 12) {
                throw new IllegalArgumentException("encoded payload too short to contain IV");
            }
            byte[] iv = new byte[12];
            System.arraycopy(all, 0, iv, 0, 12);
            byte[] cipherBytes = new byte[all.length - 12];
            System.arraycopy(all, 12, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt/parse encoded payload", e);
        }
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}

