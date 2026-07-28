package com.fpt.swp.sealhackathonbe.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;
    private final String testKeyBase64 = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "base64EncryptionKey", testKeyBase64);
        encryptionUtil.init();
    }

    @Test
    void testEncryptionDecryption_Success() {
        String originalText = "my-github-token";
        EncryptionUtil.EncryptedData encryptedData = encryptionUtil.encrypt(originalText);
        
        assertNotNull(encryptedData.getCipherText());
        assertNotNull(encryptedData.getIv());
        assertEquals(1, encryptedData.getVersion());
        
        String decryptedText = encryptionUtil.decrypt(encryptedData.getCipherText(), encryptedData.getIv(), encryptedData.getVersion());
        assertEquals(originalText, decryptedText);
    }

    @Test
    void testEncryption_RandomIVsForSameText() {
        String originalText = "my-github-token";
        EncryptionUtil.EncryptedData encryptedData1 = encryptionUtil.encrypt(originalText);
        EncryptionUtil.EncryptedData encryptedData2 = encryptionUtil.encrypt(originalText);
        
        assertNotEquals(encryptedData1.getIv(), encryptedData2.getIv());
        assertNotEquals(encryptedData1.getCipherText(), encryptedData2.getCipherText());
        
        String decryptedText1 = encryptionUtil.decrypt(encryptedData1.getCipherText(), encryptedData1.getIv(), encryptedData1.getVersion());
        String decryptedText2 = encryptionUtil.decrypt(encryptedData2.getCipherText(), encryptedData2.getIv(), encryptedData2.getVersion());
        
        assertEquals(originalText, decryptedText1);
        assertEquals(originalText, decryptedText2);
    }

    @Test
    void testDecryption_WrongVersion() {
        EncryptionUtil.EncryptedData encryptedData = encryptionUtil.encrypt("test");
        assertThrows(IllegalArgumentException.class, () -> encryptionUtil.decrypt(encryptedData.getCipherText(), encryptedData.getIv(), 2));
    }
}
