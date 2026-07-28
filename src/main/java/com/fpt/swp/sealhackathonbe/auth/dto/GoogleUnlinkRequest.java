package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Gỡ liên kết Google khỏi user hiện tại — phải xác minh lại mật khẩu local.
 */
@Getter
@Setter
@NoArgsConstructor
public class GoogleUnlinkRequest {

    @NotBlank(message = "password is required")
    private String password;
}
