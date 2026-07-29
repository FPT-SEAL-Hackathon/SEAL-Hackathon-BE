package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.settings.service.FptStudentCodePrefixService;
import com.fpt.swp.sealhackathonbe.settings.service.impl.FptStudentCodePrefixServiceImpl;
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
public class ProfileController {

    private final AuthenticationServiceImpl authService;
    private final UserRepository userRepository;
    private final FptStudentCodePrefixService fptStudentCodePrefixService;

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
            String newPhone = phone.isEmpty() ? null : phone;
            // Enforce-on-change: chỉ validate SĐT VN khi đổi giá trị (grandfather dữ liệu cũ).
            if (newPhone != null && !newPhone.equals(user.getPhone())
                    && !com.fpt.swp.sealhackathonbe.user.util.ProfileValidation.isValidVietnamesePhone(newPhone)) {
                throw new BadRequestException(
                        com.fpt.swp.sealhackathonbe.user.util.ProfileValidation.MSG_PHONE);
            }
            user.setPhone(newPhone);
        }

        // Học sinh tự sửa mã SV (chuẩn hóa). Chỉ áp đúng loại theo role, enforce-on-change + check trùng.
        boolean isFptStudent = user.getUserType() != null
                && "FPT Student".equalsIgnoreCase(user.getUserType().getTypeName());
        boolean isExternalStudentRole = user.getUserType() != null
                && "External Student".equalsIgnoreCase(user.getUserType().getTypeName());

        if (isFptStudent && request.getFptStudentCode() != null) {
            String code = request.getFptStudentCode().trim();
            String newCode = code.isEmpty() ? null : code;
            if (newCode != null && !newCode.equals(user.getFptStudentCode())) {
                if (!fptStudentCodePrefixService.isValidActiveFptStudentCode(newCode)) {
                    throw new BadRequestException(FptStudentCodePrefixServiceImpl.MSG_FPT_CODE);
                }
                if (userRepository.existsByFptStudentCodeAndIsDeletedFalseAndUserIdNot(newCode, user.getUserId())) {
                    throw new BadRequestException("FPT student code already exists.");
                }
                user.setFptStudentCode(fptStudentCodePrefixService.normalizeFptStudentCode(newCode));
            }
        }
        if (isExternalStudentRole && request.getExternalStudentCode() != null) {
            String code = request.getExternalStudentCode().trim();
            String newCode = code.isEmpty() ? null : code;
            if (newCode != null && !newCode.equals(user.getExternalStudentCode())) {
                if (!com.fpt.swp.sealhackathonbe.user.util.ProfileValidation.isValidExternalStudentCode(newCode)) {
                    throw new BadRequestException(
                            com.fpt.swp.sealhackathonbe.user.util.ProfileValidation.MSG_EXTERNAL_CODE);
                }
                if (userRepository.existsByExternalStudentCodeAndIsDeletedFalseAndUserIdNot(newCode, user.getUserId())) {
                    throw new BadRequestException("External student code already exists.");
                }
                user.setExternalStudentCode(newCode);
            }
        }

        if (request.getUniversityName() != null) {
            String universityName = request.getUniversityName().trim();
            // External Student bắt buộc có trường (điều kiện eligibility lập team/
            // đăng ký event) — không cho tự xóa trắng rồi kẹt ở bước đăng ký.
            boolean isExternalStudent = user.getUserType() != null
                    && "External Student".equalsIgnoreCase(user.getUserType().getTypeName());
            if (universityName.isBlank() && isExternalStudent) {
                throw new BadRequestException("University is required for External Student.");
            }
            user.setUniversityName(universityName.isBlank() ? null : universityName);
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
                .profileCompliant(fptStudentCodePrefixService.profileIssues(user).isEmpty())
                .profileIssues(fptStudentCodePrefixService.profileIssues(user))
                .build();
    }

    private String toApiName(String value) {
        if (value == null) return null;
        return value.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ROOT);
    }
}
