package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

public interface JwtService {
    public String generateAccessToken(User user);
    public String generateRefreshToken(User user);
    public String extractUserName(String token);
    public String extractRole(String token);
    public Date extractExpiration(String token);
    public Claims extractAllClaims(String token);
    public boolean isTokenExpired(String token);
    public boolean validateToken(String token, UserDetails userDetails);
    public Key getKey();
}
