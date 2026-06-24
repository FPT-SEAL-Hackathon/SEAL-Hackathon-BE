package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.impl.JwtServiceImpl;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.notification.service.EmailService;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private JwtServiceImpl jwtServiceImpl;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserTypeRepository userTypeRepo;

    @Autowired
    private AccountStatusRepository accountStatusRepo;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);


    public LoginResponse verify(LoginRequest request) {

        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        if (authentication.isAuthenticated()) {

            UserPrincipal userPrincipal =
                    (UserPrincipal) authentication.getPrincipal();

            User user = userPrincipal.getUser();
            // Check trạng thái tài khoản
            if ("UNVERIFIED".equalsIgnoreCase(
                    user.getAccountStatus().getStatusName())) {

                throw new IllegalStateException(
                        "Please verify your email before logging in or contact Admin support"
                );
            }

            String accessToken = jwtServiceImpl.generateAccessToken(user);

            String refreshToken = jwtServiceImpl.generateRefreshToken(user);

            String studentCode =
                    user.getFptStudentCode() != null
                            ? user.getFptStudentCode()
                            : user.getExternalStudentCode();

            UserResponse userResponse =
                    UserResponse.builder()
                            .id(user.getUserId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .userType(user.getUserType().getTypeName())
                            .studentCode(studentCode)
                            .universityName(user.getUniversityName())
                            .phone(user.getPhone())
                            .accountStatus(
                                    user.getAccountStatus().getStatusName()
                            )
                            .createdAt(user.getCreatedAt())
                            .build();

            RefreshToken tokenEntity = RefreshToken.builder()
                    .user(user)
                    .tokenHash(refreshToken)   // ⚠️ LƯU REFRESH TOKEN (KHÔNG PHẢI ACCESS)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .revokedAt(null)
                    .deviceInfo("WEB")
                    .build();

            refreshTokenRepository.save(tokenEntity);
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userResponse)
                    .build();
        }

        throw new RuntimeException("Invalid email or password");
    }

    private void createAndSendVerificationToken(User user) {

        String verificationToken =
                UUID.randomUUID().toString();

        VerificationToken tokenEntity =
                VerificationToken.builder()
                        .user(user)
                        .tokenHash(verificationToken)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(
                                LocalDateTime.now().plusHours(24)
                        )
                        .build();

        verificationTokenRepository.save(tokenEntity);

        String verifyLink =
                "http://localhost:8080/auth/verify-email?token="
                        + verificationToken;

        String subject = "Verify Your Email";

        String content =
                "Welcome to SEAL Hackathon.\n\n"
                        + "Please click the link below to verify your email:\n\n"
                        + verifyLink
                        + "\n\n"
                        + "This link will expire in 24 hours.";

        emailService.sendEmail(
                user.getEmail(),
                subject,
                content
        );
    }

    @Transactional
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(refreshToken)
                .orElseThrow(() ->
                        new RuntimeException("Token not found"));

        token.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(token); // 🔥 nên thêm
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        // Kiểm tra confirm password
        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "Password and Confirm Password do not match"
            );
        }
        // Kiểm tra email đã tồn tại
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getUserTypeId() == null) {
            throw new IllegalArgumentException("User type is required");
        }

        UserType userType = userTypeRepo
                .findById(request.getUserTypeId())
                .orElseThrow(() ->
                        new RuntimeException("User type not found"));
        AccountStatus accountStatus = accountStatusRepo
                .findByStatusName("Unverified")
                .orElseThrow(() ->
                        new RuntimeException("Account status not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setUniversityName(request.getUniversityName());
        user.setPhone(request.getPhone());
        user.setUserType(userType);
        user.setAccountStatus(accountStatus);
        user.setCreatedAt(LocalDateTime.now());
        // Student code
        UUID FPT_STUDENT_ID = UUID.fromString(
                "10000000-0000-0000-0000-000000000001"
        );

        if (userType.getUserTypeId().equals(FPT_STUDENT_ID)) {
            user.setFptStudentCode(request.getStudentCode());
        } else {
            user.setExternalStudentCode(request.getStudentCode());
        }

        user.setPasswordHash(
                encoder.encode(request.getPassword())
        );

        User savedUser = userRepo.save(user);

        String studentCode =
                savedUser.getFptStudentCode() != null
                        ? savedUser.getFptStudentCode()
                        : savedUser.getExternalStudentCode();

        createAndSendVerificationToken(savedUser);


        return UserResponse.builder()
                .id(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .userType(
                        savedUser.getUserType().getTypeName()
                )
                .studentCode(studentCode)
                .universityName(
                        savedUser.getUniversityName()
                )
                .phone(savedUser.getPhone())
                .accountStatus(
                        savedUser.getAccountStatus()
                                .getStatusName()
                )
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}


