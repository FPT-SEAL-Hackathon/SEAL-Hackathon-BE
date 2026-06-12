package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.service.JWTService;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
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

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserTypeRepository userTypeRepo;

    @Autowired
    private AccountStatusRepository accountStatusRepo;

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

            String accessToken = jwtService.generateToken(user);

            String refreshToken =
                    jwtService.generateRefreshToken(user);

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

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userResponse)
                    .build();
        }

        throw new RuntimeException("Invalid email or password");
    }
    @Transactional
    public UserResponse register(RegisterRequest request) {

        try {

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

            UserType userType = userTypeRepo
                    .findById(request.getUserTypeId())
                    .orElseThrow(() ->
                            new RuntimeException("User type not found"));
            AccountStatus accountStatus = accountStatusRepo
                    .findByStatusName("ACTIVE")
                    .orElseThrow(() ->
                            new RuntimeException("Account status not found"));

            User user = new User();
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setUniversityName(request.getUniversityName());
            user.setPhone(request.getPhone());
            user.setUserType(userType);
            user.setAccountStatus(accountStatus);

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

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}


