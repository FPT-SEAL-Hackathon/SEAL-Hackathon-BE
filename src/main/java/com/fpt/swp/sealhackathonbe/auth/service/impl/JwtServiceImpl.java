package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.RefreshTokenRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.TokenResponse;
import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.JwtService;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.utils.TokenHashUtil;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Tạo, đọc và xác thực JWT cho đăng nhập, refresh token và xác minh email.
 */
@Service
public class JwtServiceImpl implements JwtService {
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 30; // 30 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7L; // 7 ngày

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private TokenHashUtil tokenHashUtil;

    @Autowired
    private AccountStatusRepository accountStatusRepo;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * JWT:
     * Tạo access token chứa userId, email và role hiện tại của người dùng.
     */
    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getUserId());

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

    /**
     * Refresh Token:
     * Tạo token dài hạn dùng để xin access token mới.
     */
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

    /**
     * Refresh Token:
     * Từ chối token đã thu hồi/hết hạn trước khi cấp access token mới.
     */
    @Transactional
    public TokenResponse refresh(
            RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        RefreshToken tokenEntity =
                refreshTokenRepository
                        .findByTokenHash(tokenHashUtil.hash(refreshToken))
                        .orElseThrow(
                                () -> new BadCredentialsException(
                                        "Refresh token not found"
                                )
                        );

        if (tokenEntity.getRevokedAt() != null) {
            throw new BadCredentialsException(
                    "Refresh token revoked"
            );
        }

        if (tokenEntity.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new CredentialsExpiredException(
                    "Refresh token expired"
            );
        }

        User user = tokenEntity.getUser();

        // Tài khoản đã xóa/suspend không được cấp access token mới,
        // kể cả khi refresh token của phiên cũ vẫn còn hạn.
        String statusName = user.getAccountStatus() != null
                ? user.getAccountStatus().getStatusName()
                : "";
        boolean enabled = "Active".equalsIgnoreCase(statusName)
                || "Temporary".equalsIgnoreCase(statusName);
        if (Boolean.TRUE.equals(user.getIsDeleted()) || !enabled) {
            throw new BadCredentialsException("User account is not active");
        }

        String newAccessToken = generateAccessToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * JWT:
     * Lấy email chủ sở hữu token để nạp thông tin người dùng.
     */
    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * JWT:
     * Lấy role trong token để dựng quyền ROLE_*.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * JWT:
     * Lấy claim userId để nạp đúng user khi email không còn duy nhất.
     */
    public String extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * JWT:
     * Lấy thời điểm hết hạn để kiểm tra vòng đời token.
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * JWT:
     * Xác thực chữ ký trước khi tin tưởng các claim trong token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT:
     * Kiểm tra token đã quá hạn so với thời điểm hiện tại.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * JWT:
     * Token chỉ hợp lệ khi email khớp user đã nạp và chưa hết hạn.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * JWT:
     * Tạo khóa ký từ secret cấu hình để ký và xác minh token.
     */
    public Key getKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Kích hoạt tài khoản nếu token xác minh email còn hiệu lực.
     * Trả về User đã được kích hoạt để caller có thể cấp phiên đăng nhập ngay.
     */
    @Transactional
    public User verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException(
                    "Invalid verification token"
            );
        }

        VerificationToken verificationToken =
                verificationTokenRepository
                        .findByTokenHash(tokenHashUtil.hash(token))
                        .orElseThrow(
                                () -> new BadRequestException(
                                        "Invalid verification token"
                                )
                        );

        if (verificationToken.getUsedAt() != null) {
            throw new BadRequestException(
                    "Verification token already used"
            );
        }

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Verification token expired"
            );
        }

        User user = verificationToken.getUser();

        AccountStatus activeStatus =
                accountStatusRepo
                        .findByStatusNameIgnoreCase("Active")
                        .orElseThrow();

        user.setAccountStatus(activeStatus);

        verificationToken.setUsedAt(
                LocalDateTime.now()
        );

        return user;
    }
}
