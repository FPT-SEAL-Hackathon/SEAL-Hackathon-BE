package com.fpt.swp.sealhackathonbe.core.exception;

/**
 * Email đã thuộc một user hiện có nhưng thiếu phương thức đăng nhập tương ứng:
 * - Đăng ký local trong khi user chỉ có đăng nhập Google (chưa có mật khẩu).
 * KHÔNG tạo user mới — trả 409 ACCOUNT_LINK_REQUIRED kèm linkingToken ngắn hạn
 * để client xác minh quyền sở hữu (OTP email) rồi hoàn tất liên kết/thiết lập.
 */
public class AccountLinkRequiredException extends RuntimeException {

    private final String linkingToken;
    private final String email;

    public AccountLinkRequiredException(String message, String linkingToken, String email) {
        super(message);
        this.linkingToken = linkingToken;
        this.email = email;
    }

    public String getLinkingToken() {
        return linkingToken;
    }

    public String getEmail() {
        return email;
    }
}
