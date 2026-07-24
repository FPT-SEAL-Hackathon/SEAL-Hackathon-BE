package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class RepositoryTokenEncryptionService {

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
            log.error("Missing REPOSITORY_TOKEN_ENCRYPTION_KEY environment variable");
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Encryption key is missing");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64EncryptionKey.trim());
        } catch (IllegalArgumentException e) {
            log.error("REPOSITORY_TOKEN_ENCRYPTION_KEY is not a valid Base64 string");
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Encryption key is invalid");
        }

        if (keyBytes.length != 32) {
            log.error("REPOSITORY_TOKEN_ENCRYPTION_KEY must be exactly 32 bytes. Provided length: {}", keyBytes.length);
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Encryption key has invalid length");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public EncryptedData encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            throw new IllegalArgumentException("Plaintext cannot be null or empty");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            return new EncryptedData(cipherText, iv, CURRENT_FORMAT_VERSION);
        } catch (Exception e) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Encryption operation failed", e);
        }
    }

    public String decrypt(byte[] cipherText, byte[] iv, Integer version) {
        if (version == null || version != CURRENT_FORMAT_VERSION) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Unsupported encryption format version");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_TOKEN, "Decryption failed. The token data may be corrupted or tampered with.", e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class EncryptedData {
        private final byte[] cipherText;
        private final byte[] iv;
        private final int version;
    }
}
