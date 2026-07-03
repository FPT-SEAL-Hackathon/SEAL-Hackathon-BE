package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateMyProfileRequest;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Cho phép bất kỳ user đã đăng nhập nào tự xem và cập nhật hồ sơ của mình.
 * Không cần role đặc biệt, chỉ cần JWT hợp lệ.
 */
@Tag(name = "My Profile", description = "Self-service profile APIs for the authenticated user")
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthenticationServiceImpl authService;
    private final UserRepository userRepository;

    @Operation(summary = "Get current user profile")
    @GetMapping
    public ResponseEntity<UserResponse> getMe() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(toResponse(user));
    }

    @Operation(summary = "Update current user profile (fullName, phone, universityName)")
    @PutMapping
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateMyProfileRequest request) {
        User user = authService.getCurrentUser();

        if (request.getFullName() != null) {
            String fullName = request.getFullName().trim();
            if (fullName.isBlank()) {
                throw new BadRequestException("Full name must not be blank");
            }
            user.setFullName(fullName);
        }

        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            // basic validation: allow blank (clear phone) or valid format
            if (!phone.isEmpty() && !phone.matches("^[0-9+()\\-\\s]{7,20}$")) {
                throw new BadRequestException("Invalid phone number.");
            }
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        if (request.getUniversityName() != null) {
            user.setUniversityName(
                    request.getUniversityName().isBlank() ? null : request.getUniversityName().trim()
            );
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        String roleName = user.getUserType() != null ? user.getUserType().getTypeName() : null;
        String statusName = user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null;
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(toApiName(roleName))
                .roleName(roleName)
                .fptStudentCode(user.getFptStudentCode())
                .externalStudentCode(user.getExternalStudentCode())
                .universityName(user.getUniversityName())
                .phone(user.getPhone())
                .accountStatus(toApiName(statusName))
                .accountStatusName(statusName)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String toApiName(String value) {
        if (value == null) return null;
        return value.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ROOT);
    }
}
