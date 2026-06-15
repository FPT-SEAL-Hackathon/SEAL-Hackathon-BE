package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.LogoutRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.RefreshTokenRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    UserResponse register(RegisterRequest request);

    void logout(LogoutRequest request);

}