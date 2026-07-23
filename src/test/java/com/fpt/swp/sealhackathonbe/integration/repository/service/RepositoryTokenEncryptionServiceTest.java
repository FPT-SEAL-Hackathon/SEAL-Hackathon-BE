package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTokenEncryptionServiceTest {

    private RepositoryTokenEncryptionService encryptionService;
    private final String validKeyBase64 = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());

    @BeforeEach
    void setUp() {
        encryptionService = new RepositoryTokenEncryptionService();
    }

    @Test
    void testInit_MissingKey() {
        ReflectionTestUtils.setField(encryptionService, "base64EncryptionKey", null);
        assertThrows(RepositoryIntegrationException.class, () -> encryptionService.init());
    }

    @Test
    void testInit_InvalidBase64() {
        ReflectionTestUtils.setField(encryptionService, "base64EncryptionKey", "not-base-64!!!");
        assertThrows(RepositoryIntegrationException.class, () -> encryptionService.init());
    }

    @Test
    void testInit_InvalidLength() {
        String invalidLengthKey = Base64.getEncoder().encodeToString("1234567890".getBytes());
        ReflectionTestUtils.setField(encryptionService, "base64EncryptionKey", invalidLengthKey);
        assertThrows(RepositoryIntegrationException.class, () -> encryptionService.init());
    }

    @Test
    void testEncryptDecrypt_Success() {
        ReflectionTestUtils.setField(encryptionService, "base64EncryptionKey", validKeyBase64);
        encryptionService.init();

        String plaintext = "my-secret-github-token";
        RepositoryTokenEncryptionService.EncryptedData encryptedData = encryptionService.encrypt(plaintext);
        
        assertNotNull(encryptedData.getCipherText());
        assertNotNull(encryptedData.getIv());
        assertEquals(1, encryptedData.getVersion());
        assertEquals(12, encryptedData.getIv().length);

        String decryptedText = encryptionService.decrypt(encryptedData.getCipherText(), encryptedData.getIv(), encryptedData.getVersion());
        assertEquals(plaintext, decryptedText);
    }
}
