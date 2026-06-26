package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.impl.JwtServiceImpl;
import com.fpt.swp.sealhackathonbe.core.config.AppProperties;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Xử lý đăng ký, đăng nhập, lưu refresh token,
 * xác minh email và thu hồi phiên đăng xuất.
 */
@Service
public class UserService {
    private static final UUID FPT_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID EXTERNAL_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

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

    @Autowired
    private AppProperties appProperties;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    /**
     * Đăng nhập:
     * Xác thực tài khoản đã verify rồi cấp JWT và thông tin hồ sơ.
     */
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
            if ("UNVERIFIED".equalsIgnoreCase(
                    user.getAccountStatus().getStatusName())) {

                throw new IllegalStateException(
                        "Please verify your email before logging in or contact Admin support"
                );
            }

            // JWT:
            // Cấp access token ngắn hạn sau khi xác thực thành công.
            String accessToken = jwtServiceImpl.generateAccessToken(user);

            // Token làm mới:
            // Lưu refresh token để quản lý phiên và hỗ trợ logout.
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
                    .tokenHash(refreshToken)
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

    /**
     * Tạo token xác minh email một lần cho tài khoản mới đăng ký.
     */
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

        // Xác minh email được thực hiện từ frontend, nên host phải lấy từ cấu hình triển khai.
        String verifyLink = UriComponentsBuilder
                .fromUriString(appProperties.getFrontendUrl())
                .path("/verify-email")
                .queryParam("token", verificationToken)
                .build()
                .toUriString();

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

    /**
     * Đăng xuất:
     * Thu hồi refresh token để phiên hiện tại không thể refresh tiếp.
     */
    @Transactional
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(refreshToken)
                .orElseThrow(() ->
                        new RuntimeException("Token not found"));

        token.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(token);
    }

    /**
     * Đăng ký:
     * Chỉ cho tự đăng ký tài khoản student và bắt buộc xác minh email.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "Password and Confirm Password do not match"
            );
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        UserType userType = userTypeRepo
                .findById(request.getUserTypeId())
                .orElseThrow(() ->
                        new RuntimeException("User type not found"));

        // RBAC:
        // Chặn tự đăng ký role đặc quyền như ORGANIZER/JUDGE từ API public.
        if (!FPT_STUDENT_ID.equals(userType.getUserTypeId())
                && !EXTERNAL_STUDENT_ID.equals(userType.getUserTypeId())) {
            throw new RuntimeException("This user type cannot be self-registered");
        }

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

        if (userType.getUserTypeId().equals(FPT_STUDENT_ID)) {
            user.setFptStudentCode(request.getStudentCode());
        } else {
            user.setExternalStudentCode(request.getStudentCode());
        }

        // Mật khẩu:
        // Mã hóa mật khẩu trước khi lưu để không ghi plaintext vào database.
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
