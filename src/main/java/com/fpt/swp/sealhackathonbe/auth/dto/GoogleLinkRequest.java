package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hoàn tất liên kết Google vào user hiện có.
 * password chỉ bắt buộc khi chưa xác minh bằng OTP.
 */
@Getter
@Setter
@NoArgsConstructor
public class GoogleLinkRequest {

    @NotBlank(message = "linkingToken is required")
    private String linkingToken;

    private String password;
}
