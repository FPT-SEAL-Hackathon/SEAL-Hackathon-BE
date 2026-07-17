package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Yêu cầu gửi OTP xác minh cho phiên liên kết tài khoản.
 * OTP luôn được gửi tới email của user đích trong ticket — client không chỉ định email.
 */
@Getter
@Setter
@NoArgsConstructor
public class LinkOtpSendRequest {

    @NotBlank(message = "linkingToken is required")
    private String linkingToken;
}
