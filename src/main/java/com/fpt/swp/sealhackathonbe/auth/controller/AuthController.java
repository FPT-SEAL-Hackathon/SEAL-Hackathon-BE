/**
 * Authentication Controller
 *
 * Chịu trách nhiệm xử lý:
 * - Đăng ký tài khoản
 * - Đăng nhập hệ thống
 * - Trả JWT cho client sau khi xác thực thành công
 */
package com.fpt.swp.sealhackathonbe.auth.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.*;
import com.fpt.swp.sealhackathonbe.auth.service.impl.JwtServiceImpl;
import com.fpt.swp.sealhackathonbe.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtServiceImpl jwtServiceImpl;
    // API đăng ký tài khoản mới
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        System.out.println("REGISTER CALLED");
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){
        LoginResponse response = userService.verify(request);
        return ResponseEntity.ok(response);
    }
//    @PostMapping("/refresh")
//    public ResponseEntity<LoginResponse> refresh(
//            @Valid @RequestBody RefreshTokenRequest request
//    ) {
//        LoginResponse response = authService.refresh(request);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String accessToken = authHeader.substring(7);

        userService.logout(request.getRefreshToken());

        return ResponseEntity.ok("Logout successful");
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                jwtServiceImpl.refresh(request)
        );
    }
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {

        jwtServiceImpl.verifyEmail(token);

        return ResponseEntity.ok(
                "Email verified successfully"
        );
    }
}
