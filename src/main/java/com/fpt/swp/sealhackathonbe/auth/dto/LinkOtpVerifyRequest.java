package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Xác minh OTP cho phiên liên kết tài khoản.
 */
@Getter
@Setter
@NoArgsConstructor
public class LinkOtpVerifyRequest {

    @NotBlank(message = "linkingToken is required")
    private String linkingToken;

    @NotBlank(message = "otp is required")
    private String otp;
}
