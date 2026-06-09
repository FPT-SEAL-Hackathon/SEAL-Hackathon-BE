/**
 * Authentication Controller
 *
 * Chịu trách nhiệm xử lý:
 * - Đăng ký tài khoản
 * - Đăng nhập hệ thống
 * - Trả JWT cho client sau khi xác thực thành công
 */
package com.fpt.swp.sealhackathonbe.auth.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")

public class AuthController {

    @Autowired
    private UserService userService;
    // API đăng ký tài khoản mới
    @PostMapping("/register")
    public UserResponse register(
            @Valid
            @RequestBody RegisterRequest request
    ) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody LoginRequest request
    ) {
        return userService.verify(request);
    }
}
