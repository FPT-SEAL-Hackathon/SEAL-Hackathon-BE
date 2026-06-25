package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

/**
 * Định nghĩa các thao tác tạo, đọc và xác thực JWT.
 */
public interface JwtService {

    /**
     * Tạo access token phục vụ phân quyền API.
     */
    public String generateAccessToken(User user);

    /**
     * Tạo refresh token để duy trì phiên đăng nhập.
     */
    public String generateRefreshToken(User user);

    /**
     * Lấy email chủ thể từ JWT.
     */
    public String extractUserName(String token);

    /**
     * Lấy role claim để dựng quyền ROLE_*.
     */
    public String extractRole(String token);

    /**
     * Lấy thời điểm hết hạn của JWT.
     */
    public Date extractExpiration(String token);

    /**
     * Đọc claims sau khi xác thực chữ ký token.
     */
    public Claims extractAllClaims(String token);

    /**
     * Kiểm tra JWT đã hết hạn hay chưa.
     */
    public boolean isTokenExpired(String token);

    /**
     * Xác thực email và hạn token với user đã nạp.
     */
    public boolean validateToken(String token, UserDetails userDetails);

    /**
     * Lấy khóa ký dùng cho các thao tác JWT.
     */
    public Key getKey();
}
