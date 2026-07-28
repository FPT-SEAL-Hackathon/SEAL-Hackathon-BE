package com.fpt.swp.sealhackathonbe.user.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileValidationTest {

    @Test
    void fptStudentCode_acceptsSE_SS_SA_plus6Digits() {
        assertTrue(ProfileValidation.isValidFptStudentCode("SE123456"));
        assertTrue(ProfileValidation.isValidFptStudentCode("SS200010"));
        assertTrue(ProfileValidation.isValidFptStudentCode("SA000001"));
    }

    @Test
    void fptStudentCode_rejectsWrongFormat() {
        assertFalse(ProfileValidation.isValidFptStudentCode(null));
        assertFalse(ProfileValidation.isValidFptStudentCode("SE12345"));   // 5 số
        assertFalse(ProfileValidation.isValidFptStudentCode("SE1234567")); // 7 số
        assertFalse(ProfileValidation.isValidFptStudentCode("SB123456"));  // prefix sai
        assertFalse(ProfileValidation.isValidFptStudentCode("se123456"));  // thường
        assertFalse(ProfileValidation.isValidFptStudentCode("SEABCDEF"));  // không số
    }

    @Test
    void externalStudentCode_lenient() {
        assertTrue(ProfileValidation.isValidExternalStudentCode("EXT-200015"));
        assertTrue(ProfileValidation.isValidExternalStudentCode("hcmut_01"));
        assertFalse(ProfileValidation.isValidExternalStudentCode("ab"));   // < 3
        assertFalse(ProfileValidation.isValidExternalStudentCode(null));
        assertFalse(ProfileValidation.isValidExternalStudentCode("-abc")); // bắt đầu không alphanumeric
    }

    @Test
    void vietnamesePhone_acceptsValidMobile() {
        assertTrue(ProfileValidation.isValidVietnamesePhone("0912345678"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("+84912345678"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("0387654321"));
        assertTrue(ProfileValidation.isValidVietnamesePhone("09 1234 5678")); // có khoảng trắng
    }

    @Test
    void vietnamesePhone_rejectsInvalid() {
        assertFalse(ProfileValidation.isValidVietnamesePhone(null));
        assertFalse(ProfileValidation.isValidVietnamesePhone("091234567"));   // 9 số
        assertFalse(ProfileValidation.isValidVietnamesePhone("0212345678"));  // đầu số 2 (không hợp lệ)
        assertFalse(ProfileValidation.isValidVietnamesePhone("1234567890"));
        assertFalse(ProfileValidation.isValidVietnamesePhone("+849123456789")); // dư số
    }
}
