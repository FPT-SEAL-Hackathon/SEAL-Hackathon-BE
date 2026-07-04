package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Liên kết tài khoản OAuth tạm vào tài khoản local có sẵn.
 * Bắt buộc xác minh mật khẩu local — không auto-link theo email.
 */
@Getter
@Setter
public class LinkLocalAccountRequest {

    @NotBlank(message = "Target email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String targetEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
