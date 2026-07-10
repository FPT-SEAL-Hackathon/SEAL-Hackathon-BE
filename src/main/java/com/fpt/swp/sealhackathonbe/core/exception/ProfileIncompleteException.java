package com.fpt.swp.sealhackathonbe.core.exception;

/**
 * Hồ sơ chưa hoàn thiện (user TEMPORARY hoặc thiếu student code)
 * nên chưa được đăng ký sự kiện. Trả 403 PROFILE_INCOMPLETE.
 */
public class ProfileIncompleteException extends RuntimeException {

    public ProfileIncompleteException(String message) {
        super(message);
    }
}
