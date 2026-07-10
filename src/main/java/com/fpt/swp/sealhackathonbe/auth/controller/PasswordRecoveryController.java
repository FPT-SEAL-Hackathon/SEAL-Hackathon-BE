package com.fpt.swp.sealhackathonbe.auth.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.ForgotPasswordRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.ResetPasswordRequest;
import com.fpt.swp.sealhackathonbe.auth.service.impl.PasswordRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Quên/đặt lại mật khẩu cho tài khoản LOCAL.
 * forgot-password luôn trả thông báo chung, không tiết lộ email có tồn tại hay không.
 */
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private static final String GENERIC_FORGOT_MESSAGE =
            "If an account with local login exists for this email, a reset link has been sent.";

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordRecoveryService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", GENERIC_FORGOT_MESSAGE
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordRecoveryService.resetPassword(
                request.getToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password has been reset successfully."
        ));
    }
}
