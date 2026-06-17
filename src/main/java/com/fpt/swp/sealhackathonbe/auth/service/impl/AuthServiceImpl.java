package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.*;
import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.AuthService;
import com.fpt.swp.sealhackathonbe.core.exception.ExceptionPrivate.AccountNotVerifiedException;
import com.fpt.swp.sealhackathonbe.notification.service.EmailService;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import com.fpt.swp.sealhackathonbe.core.utils.TokenHashUtil;
import com.fpt.swp.sealhackathonbe.core.exception.ApiException;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final UUID FPT_STUDENT_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID ACCOUNT_STATUS_UNVERIFIED_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000006"
    );

    @Autowired
    private TokenHashUtil tokenHashUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTServiceImpl jwtServiceImpl;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserTypeRepository userTypeRepo;

    @Autowired
    private AccountStatusRepository accountStatusRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenServiceImpl verificationTokenServiceImpl;
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizeEmail(request.getEmail()),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        User user = userPrincipal.getUser();

        if ("UNVERIFIED".equalsIgnoreCase(
                user.getUserType().getTypeName())) {

            throw new AccountNotVerifiedException(
                    "Please verify your email first"
            );
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String rawRefreshToken = request.getRefreshToken();
        User user = validateRefreshToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHashUtil.hash(rawRefreshToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password and Confirm Password do not match");
        }

        String email = normalizeEmail(request.getEmail());

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserType userType = userTypeRepo
                .findById(request.getUserTypeId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "User type not found"));
        AccountStatus accountStatus = accountStatusRepo
                .findById(ACCOUNT_STATUS_UNVERIFIED_ID)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Account status not found"));

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setUniversityName(request.getUniversityName().trim());
        user.setPhone(request.getPhone().trim());
        user.setUserType(userType);
        user.setAccountStatus(accountStatus);
        user.setCreatedAt(LocalDateTime.now());

        if (userType.getUserTypeId().equals(FPT_STUDENT_ID)) {
            user.setFptStudentCode(request.getStudentCode().trim());
        } else {
            user.setExternalStudentCode(request.getStudentCode().trim());
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepo.save(user);
        VerificationToken token =
                verificationTokenServiceImpl.createToken(savedUser);

        sendVerificationEmail(savedUser, token.getTokenHash());
        return toUserResponse(savedUser);
    }
    @Override
    public void sendVerificationEmail(User user, String token) {

        String verifyUrl =
                "http://localhost:8080/api/auth/verify-email?token="
                        + token;

        String content =
                "Welcome!\n\n"
                        + "Please verify your email by clicking link below:\n"
                        + verifyUrl;

        emailService.sendEmail(
                user.getEmail(),
                "Verify your account",
                content
        );
    }

    @Transactional
    @Override
    public void logout(LogoutRequest request) {
        String tokenHash = tokenHashUtil.hash(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(token);
                    }
                });
    }

    private LoginResponse issueTokens(User user) {
        ensureUserCanUseAuth(user);

        String accessToken = jwtServiceImpl.generateAccessToken(user);
        String refreshToken = jwtServiceImpl.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    private User validateRefreshToken(String rawRefreshToken) {
        try {
            String email = normalizeEmail(jwtServiceImpl.extractUserName(rawRefreshToken));
            if (email == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            User user = userRepo.findByEmailIgnoreCase(email);

            if (user == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            UserDetails userDetails = new UserPrincipal(user);
            if (!jwtServiceImpl.validateToken(rawRefreshToken, userDetails, JWTServiceImpl.TOKEN_TYPE_REFRESH)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHashUtil.hash(rawRefreshToken))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

            if (!storedToken.isActive()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            ensureUserCanUseAuth(user);
            return user;
        } catch (JwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    private void saveRefreshToken(User user, String rawRefreshToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHashUtil.hash(rawRefreshToken));
        refreshToken.setIssuedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(
                LocalDateTime.ofInstant(jwtServiceImpl.extractExpiration(rawRefreshToken).toInstant(), ZoneId.systemDefault())
        );

        refreshTokenRepository.save(refreshToken);
    }

    private void ensureUserCanUseAuth(User user) {
        UserPrincipal principal = new UserPrincipal(user);

        if (!principal.isEnabled()
                || !principal.isAccountNonExpired()
                || !principal.isAccountNonLocked()
                || !principal.isCredentialsNonExpired()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Account is not allowed to authenticate");
        }
    }

    private UserResponse toUserResponse(User user) {
        String studentCode = user.getFptStudentCode() != null
                ? user.getFptStudentCode()
                : user.getExternalStudentCode();

        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userTypeId(user.getUserType().getUserTypeId())
                .userType(user.getUserType().getTypeName())
                .studentCode(studentCode)
                .universityName(user.getUniversityName())
                .phone(user.getPhone())
                .accountStatusId(user.getAccountStatus().getStatusId())
                .accountStatus(user.getAccountStatus().getStatusName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
