package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu yêu cầu gửi lại email xác minh tài khoản.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    public String getEmail() {
        return email;
    }
}
