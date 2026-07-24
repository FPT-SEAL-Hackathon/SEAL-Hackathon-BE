package com.fpt.swp.sealhackathonbe.core.utils;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int CURRENT_FORMAT_VERSION = 1;

    @Value("${REPOSITORY_TOKEN_ENCRYPTION_KEY:}")
    private String base64EncryptionKey;

    private SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        if (base64EncryptionKey == null || base64EncryptionKey.trim().isEmpty()) {
            throw new IllegalStateException("REPOSITORY_TOKEN_ENCRYPTION_KEY environment variable is missing or empty.");
        }
        
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64EncryptionKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("REPOSITORY_TOKEN_ENCRYPTION_KEY must be a valid Base64 encoded string.");
        }

        if (keyBytes.length != 32) {
            throw new IllegalStateException("REPOSITORY_TOKEN_ENCRYPTION_KEY must be exactly 32 bytes (256 bits) long.");
        }
        
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public EncryptedData encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            throw new IllegalArgumentException("Plaintext cannot be null or empty");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Generate a random 12-byte IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            return new EncryptedData(
                    Base64.getEncoder().encodeToString(cipherText),
                    Base64.getEncoder().encodeToString(iv),
                    CURRENT_FORMAT_VERSION
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String base64CipherText, String base64Iv, int version) {
        if (version != CURRENT_FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported encryption format version: " + version);
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = Base64.getDecoder().decode(base64Iv);
            byte[] cipherText = Base64.getDecoder().decode(base64CipherText);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed. The data may have been tampered with or the key is incorrect.", e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class EncryptedData {
        private final String cipherText;
        private final String iv;
        private final int version;
    }
}
