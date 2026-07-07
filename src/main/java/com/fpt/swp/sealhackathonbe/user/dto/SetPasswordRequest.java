package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Đặt mật khẩu local. Với tài khoản đã có mật khẩu local thì phải kèm
 * currentPassword; tài khoản OAuth-only đặt lần đầu thì không cần.
 */
@Getter
@Setter
public class SetPasswordRequest {

    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
