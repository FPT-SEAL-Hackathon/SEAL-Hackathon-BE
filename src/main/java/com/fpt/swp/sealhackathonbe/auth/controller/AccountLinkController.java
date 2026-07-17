package com.fpt.swp.sealhackathonbe.auth.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.GoogleLinkRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.GoogleUnlinkRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LinkOtpSendRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LinkOtpVerifyRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.SetupPasswordRequest;
import com.fpt.swp.sealhackathonbe.auth.service.impl.AccountLinkService;
import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Liên kết Google <-> tài khoản local theo nguyên tắc một người một bản ghi Users:
 * - google/link: gắn định danh Google vào user hiện có (xác minh mật khẩu hoặc OTP).
 * - google/unlink: gỡ định danh Google (yêu cầu đang đăng nhập + mật khẩu local).
 * - link/send-otp + link/verify-otp: OTP email cho phiên liên kết.
 * - local/setup-password: user Google-only thiết lập mật khẩu (sau OTP).
 * Các endpoint dùng linkingToken ngắn hạn do backend phát — client không gửi UserID.
 */
@Tag(name = "Account Linking", description = "Link Google and local sign-in methods to one user")
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AccountLinkController {

    private final AccountLinkService accountLinkService;
    private final AuthenticationServiceImpl authenticationService;
    private final UserService userService;

    @Operation(summary = "Complete linking a Google identity into the existing account")
    @PostMapping("/google/link")
    public ResponseEntity<LoginResponse> googleLink(
            @Valid @RequestBody GoogleLinkRequest request
    ) {
        User user = accountLinkService.completeGoogleLink(
                request.getLinkingToken(),
                request.getPassword()
        );
        return ResponseEntity.ok(userService.issueSession(user));
    }

    @Operation(summary = "Unlink the Google identity from the current account (password required)")
    @PostMapping("/google/unlink")
    public ResponseEntity<Map<String, Object>> googleUnlink(
            @Valid @RequestBody GoogleUnlinkRequest request
    ) {
        accountLinkService.unlinkGoogle(
                authenticationService.getCurrentUser(),
                request.getPassword()
        );
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Google login has been unlinked"
        ));
    }

    @Operation(summary = "Send a verification code (OTP) for an account-linking session")
    @PostMapping("/link/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @Valid @RequestBody LinkOtpSendRequest request
    ) {
        accountLinkService.sendOtp(request.getLinkingToken());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "A verification code has been sent to the account email"
        ));
    }

    @Operation(summary = "Verify the OTP for an account-linking session")
    @PostMapping("/link/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @Valid @RequestBody LinkOtpVerifyRequest request
    ) {
        accountLinkService.verifyOtp(request.getLinkingToken(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code accepted"
        ));
    }

    @Operation(summary = "Set up a local password for an existing Google-only account (after OTP)")
    @PostMapping("/local/setup-password")
    public ResponseEntity<LoginResponse> setupPassword(
            @Valid @RequestBody SetupPasswordRequest request
    ) {
        User user = accountLinkService.setupLocalPassword(
                request.getLinkingToken(),
                request.getPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(userService.issueSession(user));
    }
}
