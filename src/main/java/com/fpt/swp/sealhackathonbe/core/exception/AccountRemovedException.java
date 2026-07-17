package com.fpt.swp.sealhackathonbe.core.exception;

/**
 * Tài khoản đã bị organizer xóa cứng khỏi hệ thống (còn tombstone):
 * báo cho user biết cần tạo tài khoản mới thay vì trả "sai mật khẩu".
 */
public class AccountRemovedException extends RuntimeException {

    public AccountRemovedException(String message) {
        super(message);
    }
}
