package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.RefreshTokenRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.TokenResponse;
import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.JwtService;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 2;
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private AccountStatusRepository accountStatusRepo;
    @Value("${jwt.secret}")
    private String secretKey;

    // 🔥 ACCESS TOKEN
    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getUserId());

        // 🔥 ADD ROLE (QUAN TRỌNG)
        String role = user.getUserType()
                .getTypeName()
                .replace(" ", "_")
                .toUpperCase();

        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)
                )
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // 🔥 REFRESH TOKEN (giữ nhẹ, không cần role)
    public String generateRefreshToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION)
                )
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // PARSE JWT
    // =========================

    @Transactional
    public TokenResponse refresh(
            RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        RefreshToken tokenEntity =
                refreshTokenRepository
                        .findByTokenHash(refreshToken)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Refresh token not found"
                                )
                        );

        if (tokenEntity.getRevokedAt() != null) {
            throw new RuntimeException(
                    "Refresh token revoked"
            );
        }

        if (tokenEntity.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Refresh token expired"
            );
        }

        User user = tokenEntity.getUser();

        String newAccessToken = generateAccessToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }
    // =========================
    // Các Hàm phụ ở dưới
    // =========================
    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // =========================
    // KEY
    // =========================

    public Key getKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    @Transactional
    public void verifyEmail(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository
                        .findByTokenHash(token)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Invalid verification token"
                                )
                        );

        if (verificationToken.getUsedAt() != null) {
            throw new RuntimeException(
                    "Verification token already used"
            );
        }

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Verification token expired"
            );
        }

        User user = verificationToken.getUser();

        AccountStatus activeStatus =
                accountStatusRepo
                        .findByStatusName("ACTIVE")
                        .orElseThrow();

        user.setAccountStatus(activeStatus);

        verificationToken.setUsedAt(
                LocalDateTime.now()
        );
    }
}