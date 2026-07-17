package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thiết lập mật khẩu local cho user Google-only hiện có
 * (sau khi OTP của phiên liên kết đã được xác minh).
 */
@Getter
@Setter
@NoArgsConstructor
public class SetupPasswordRequest {

    @NotBlank(message = "linkingToken is required")
    private String linkingToken;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;
}
