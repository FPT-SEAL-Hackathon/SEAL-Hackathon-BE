package com.fpt.swp.sealhackathonbe.auth.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.LogoutRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.OAuth2ExchangeRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.RefreshTokenRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.ResendVerificationEmailRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.TokenResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.auth.oauth.OAuthCodeStore;
import com.fpt.swp.sealhackathonbe.auth.service.impl.JwtServiceImpl;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cung cấp các API xác thực: đăng ký, đăng nhập,
 * làm mới token, đăng xuất và xác minh email.
 */
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtServiceImpl jwtServiceImpl;

    @Autowired
    private OAuthCodeStore oauthCodeStore;

    @Autowired
    private UserRepository userRepository;

    /**
     * Đăng ký tài khoản mới và khởi tạo luồng xác minh email.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gửi lại email xác minh nếu tài khoản còn cần xác minh.
     */
    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request
    ) {
        userService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(
                "If the account needs verification, a verification email has been sent"
        );
    }

    /**
     * Xác thực email/mật khẩu và trả về JWT cho tài khoản hợp lệ.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = userService.verify(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng xuất bằng cách thu hồi refresh token của phiên hiện tại.
     */
    // Bo @RequestHeader("Authorization") bat buoc: no khong duoc dung trong than method,
    // nhung thieu header thi Spring nem MissingRequestHeaderException -> khong co handler
    // rieng -> HTTP 500 tren mot luong rat de xay ra (logout khi token da het han).
    // Logout la idempotent nen khong can header.
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutRequest request) {

        userService.logout(request.getRefreshToken());

        return ResponseEntity.ok("Logout successful");
    }

    /**
     * Refresh Token:
     * Chỉ cấp access token mới khi refresh token còn hiệu lực.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                jwtServiceImpl. refresh(request)
        );
    }

    /**
     * OAuth:
     * Đổi code một lần (phát sau khi Google xác thực xong) lấy phiên đăng nhập.
     * Trả về đúng hình dạng LoginResponse như đăng nhập local.
     */
    @PostMapping("/oauth2/exchange")
    public ResponseEntity<LoginResponse> exchangeOAuthCode(
            @Valid @RequestBody OAuth2ExchangeRequest request) {

        var userId = oauthCodeStore.consume(request.getCode());
        if (userId == null) {
            throw new BadCredentialsException("Invalid or expired OAuth exchange code");
        }

        User user = userRepository
                .findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid or expired OAuth exchange code"));

        return ResponseEntity.ok(userService.issueSession(user));
    }

    /**
     * Xác minh email để kích hoạt tài khoản và cấp phiên đăng nhập ngay sau đó.
     * Frontend lưu token nhận được và redirect vào dashboard mà không cần nhập lại mật khẩu.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<LoginResponse> verifyEmail(
            @RequestParam String token) {

        User user = jwtServiceImpl.verifyEmail(token);

        return ResponseEntity.ok(userService.issueSession(user));
    }
}
