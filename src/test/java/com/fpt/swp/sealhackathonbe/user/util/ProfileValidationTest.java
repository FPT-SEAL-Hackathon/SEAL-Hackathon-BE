package com.fpt.swp.sealhackathonbe.user.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileValidationTest {

    @Test
    void fptStudentCode_acceptsUppercasePrefixPlus6Digits() {
        assertTrue(ProfileValidation.isValidFptStudentCode("SE123456"));
        assertTrue(ProfileValidation.isValidFptStudentCode("AI200010"));
        assertTrue(ProfileValidation.isValidFptStudentCode("SB000001"));
    }

    @Test
    void fptStudentCode_rejectsWrongShape() {
        assertFalse(ProfileValidation.isValidFptStudentCode(null));
        assertFalse(ProfileValidation.isValidFptStudentCode("SE12345"));
        assertFalse(ProfileValidation.isValidFptStudentCode("SE1234567"));
        assertFalse(ProfileValidation.isValidFptStudentCode("se123456"));
        assertFalse(ProfileValidation.isValidFptStudentCode("SEABCDEF"));
    }

    @Test
    void externalStudentCode_lenient() {
        assertTrue(ProfileValidation.isValidExternalStudentCode("EXT-200015"));
        assertTrue(ProfileValidation.isValidExternalStudentCode("hcmut_01"));
        assertFalse(ProfileValidation.isValidExternalStudentCode("ab"));
        assertFalse(ProfileValidation.isValidExternalStudentCode(null));
        assertFalse(ProfileValidation.isValidExternalStudentCode("-abc"));
    }

    @Test
    void vietnamesePhone_acceptsValidMobile() {
        assertTrue(ProfileValidation.isValidVietnamesePhone("0912345678"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("+84912345678"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("0387654321"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("09 1234 5678"));
    }

    @Test
    void vietnamesePhone_rejectsInvalid() {
        assertFalse(ProfileValidation.isValidVietnamesePhone(null));
        assertFalse(ProfileValidation.isValidVietnamesePhone("091234567"));
        assertFalse(ProfileValidation.isValidVietnamesePhone("0212345678"));
        assertFalse(ProfileValidation.isValidVietnamesePhone("1234567890"));
        assertFalse(ProfileValidation.isValidVietnamesePhone("+849123456789"));
    }
}
