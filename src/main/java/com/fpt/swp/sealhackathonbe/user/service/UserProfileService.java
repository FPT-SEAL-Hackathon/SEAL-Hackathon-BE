package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.ProfileConflictException;
import com.fpt.swp.sealhackathonbe.user.dto.CompleteProfileRequest;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import com.fpt.swp.sealhackathonbe.user.util.ProfileValidation;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hoàn thiện hồ sơ cho user hiện tại (đặc biệt là user OAuth TEMPORARY).
 */
@Service
public class UserProfileService {

    private static final String ROLE_FPT_STUDENT = "FPT_STUDENT";
    private static final String ROLE_EXTERNAL_STUDENT = "EXTERNAL_STUDENT";

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final UserService userService;

    public UserProfileService(
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            AccountStatusRepository accountStatusRepository,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User user) {
        return userService.toUserResponse(user);
    }

    /**
     * Hoàn thiện hồ sơ:
     * - SUSPENDED/REJECTED bị chặn.
     * - Chỉ tự chọn được role student (không ORGANIZER/JUDGE, không ADMIN).
     * - Trùng email/phone/student code với tài khoản khác thì trả
     *   409 PROFILE_CONFLICT và KHÔNG cập nhật gì (không auto-merge).
     * - Thành công thì accountStatus chuyển sang ACTIVE.
     */
    @Transactional
    public UserResponse completeProfile(User currentUser, CompleteProfileRequest request) {

        String statusName = currentUser.getAccountStatus() != null
                ? currentUser.getAccountStatus().getStatusName()
                : "";
        if ("Suspended".equalsIgnoreCase(statusName) || "Rejected".equalsIgnoreCase(statusName)) {
            throw new AccessDeniedException("This account is not allowed to update its profile.");
        }

        String normalizedRole = normalize(request.getRole());
        if (!ROLE_FPT_STUDENT.equals(normalizedRole) && !ROLE_EXTERNAL_STUDENT.equals(normalizedRole)) {
            throw new BadRequestException("Only FPT_STUDENT or EXTERNAL_STUDENT can be selected.");
        }

        String fptStudentCode = trimToNull(request.getFptStudentCode());
        String externalStudentCode = trimToNull(request.getExternalStudentCode());
        String universityName = trimToNull(request.getUniversityName());
        String phone = trimToNull(request.getPhone());

        if (ROLE_FPT_STUDENT.equals(normalizedRole) && fptStudentCode == null) {
            throw new BadRequestException("FPT student code is required for FPT Student.");
        }
        if (ROLE_EXTERNAL_STUDENT.equals(normalizedRole)
                && (externalStudentCode == null || universityName == null)) {
            throw new BadRequestException(
                    "External student code and university name are required for External Student."
            );
        }
        // Hoàn thiện hồ sơ lần đầu → áp định dạng chuẩn ngay (không có gì để grandfather).
        if (phone != null && !ProfileValidation.isValidVietnamesePhone(phone)) {
            throw new BadRequestException(ProfileValidation.MSG_PHONE);
        }
        if (fptStudentCode != null && !ProfileValidation.isValidFptStudentCode(fptStudentCode)) {
            throw new BadRequestException(ProfileValidation.MSG_FPT_CODE);
        }
        if (externalStudentCode != null && !ProfileValidation.isValidExternalStudentCode(externalStudentCode)) {
            throw new BadRequestException(ProfileValidation.MSG_EXTERNAL_CODE);
        }

        // Phát hiện trùng hồ sơ với tài khoản KHÁC; nếu trùng thì không cập nhật.
        List<String> conflictFields = detectConflicts(
                currentUser, phone, normalizedRole, fptStudentCode, externalStudentCode
        );
        if (!conflictFields.isEmpty()) {
            throw new ProfileConflictException(
                    "Thông tin tài khoản có thể đã tồn tại. Bạn có thể liên kết tài khoản sau khi xác minh.",
                    conflictFields,
                    true
            );
        }

        UserType userType = resolveUserType(normalizedRole);
        AccountStatus activeStatus = accountStatusRepository
                .findByStatusNameIgnoreCase("Active")
                .orElseThrow(() -> new IllegalStateException("Active account status not found"));

        currentUser.setFullName(request.getFullName().trim());
        currentUser.setPhone(phone);
        currentUser.setUserType(userType);
        if (ROLE_FPT_STUDENT.equals(normalizedRole)) {
            currentUser.setFptStudentCode(fptStudentCode);
            currentUser.setExternalStudentCode(null);
            currentUser.setUniversityName(null);
        } else {
            currentUser.setFptStudentCode(null);
            currentUser.setExternalStudentCode(externalStudentCode);
            currentUser.setUniversityName(universityName);
        }
        currentUser.setAccountStatus(activeStatus);

        User saved = userRepository.save(currentUser);
        return userService.toUserResponse(saved);
    }

    private List<String> detectConflicts(
            User currentUser,
            String phone,
            String normalizedRole,
            String fptStudentCode,
            String externalStudentCode
    ) {
        List<String> conflictFields = new ArrayList<>();

        if (currentUser.getEmail() != null
                && userRepository.existsByEmailAndIsDeletedFalseAndUserIdNot(
                        currentUser.getEmail(), currentUser.getUserId())) {
            conflictFields.add("email");
        }
        if (phone != null
                && userRepository.existsByPhoneAndIsDeletedFalseAndUserIdNot(
                        phone, currentUser.getUserId())) {
            conflictFields.add("phone");
        }
        if (ROLE_FPT_STUDENT.equals(normalizedRole)
                && fptStudentCode != null
                && userRepository.existsByFptStudentCodeAndIsDeletedFalseAndUserIdNot(
                        fptStudentCode, currentUser.getUserId())) {
            conflictFields.add("fptStudentCode");
        }
        if (ROLE_EXTERNAL_STUDENT.equals(normalizedRole)
                && externalStudentCode != null
                && userRepository.existsByExternalStudentCodeAndIsDeletedFalseAndUserIdNot(
                        externalStudentCode, currentUser.getUserId())) {
            conflictFields.add("externalStudentCode");
        }

        return conflictFields;
    }

    private UserType resolveUserType(String normalizedRole) {
        return userTypeRepository.findAll().stream()
                .filter(type -> normalize(type.getTypeName()).equals(normalizedRole))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid user role."));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    @Transactional
    public UserResponse updateProfile(User currentUser, com.fpt.swp.sealhackathonbe.user.dto.UpdateProfileRequest request) {
        String statusName = currentUser.getAccountStatus() != null
                ? currentUser.getAccountStatus().getStatusName()
                : "";
        if ("Suspended".equalsIgnoreCase(statusName) || "Rejected".equalsIgnoreCase(statusName)) {
            throw new AccessDeniedException("This account is not allowed to update its profile.");
        }

        String phone = trimToNull(request.getPhone());
        String role = currentUser.getUserType().getTypeName().trim().replace(" ", "_").toUpperCase();
        boolean isFpt = "FPT_STUDENT".equals(role) || "ROLE_FPT_STUDENT".equals(role);

        String fptStudentCode = isFpt ? trimToNull(request.getFptStudentCode()) : null;
        String externalStudentCode = !isFpt ? trimToNull(request.getExternalStudentCode()) : null;

        // Enforce định dạng chuẩn CHỈ khi field thực sự thay đổi (grandfather dữ liệu cũ:
        // user chưa sửa thì không bị chặn, nhưng đã sửa thì giá trị mới phải đúng chuẩn).
        if (phone != null && !phone.equals(currentUser.getPhone())
                && !ProfileValidation.isValidVietnamesePhone(phone)) {
            throw new BadRequestException(ProfileValidation.MSG_PHONE);
        }
        if (fptStudentCode != null && !fptStudentCode.equals(currentUser.getFptStudentCode())
                && !ProfileValidation.isValidFptStudentCode(fptStudentCode)) {
            throw new BadRequestException(ProfileValidation.MSG_FPT_CODE);
        }
        if (externalStudentCode != null && !externalStudentCode.equals(currentUser.getExternalStudentCode())
                && !ProfileValidation.isValidExternalStudentCode(externalStudentCode)) {
            throw new BadRequestException(ProfileValidation.MSG_EXTERNAL_CODE);
        }

        // Check conflicts for existing phone or student codes
        List<String> conflictFields = new ArrayList<>();
        if (phone != null && userRepository.existsByPhoneAndIsDeletedFalseAndUserIdNot(phone, currentUser.getUserId())) {
            conflictFields.add("phone");
        }
        if (fptStudentCode != null && userRepository.existsByFptStudentCodeAndIsDeletedFalseAndUserIdNot(fptStudentCode, currentUser.getUserId())) {
            conflictFields.add("fptStudentCode");
        }
        if (externalStudentCode != null && userRepository.existsByExternalStudentCodeAndIsDeletedFalseAndUserIdNot(externalStudentCode, currentUser.getUserId())) {
            conflictFields.add("externalStudentCode");
        }
        if (!conflictFields.isEmpty()) {
            throw new ProfileConflictException("Thông tin tài khoản đã tồn tại.", conflictFields, true);
        }

        currentUser.setFullName(request.getFullName().trim());
        currentUser.setPhone(phone);
        currentUser.setFptStudentCode(fptStudentCode);
        currentUser.setExternalStudentCode(externalStudentCode);
        currentUser.setUniversityName(trimToNull(request.getUniversityName()));
        currentUser.setBio(trimToNull(request.getBio()));
        currentUser.setGithub(trimToNull(request.getGithub()));
        currentUser.setPortfolio(trimToNull(request.getPortfolio()));

        User saved = userRepository.save(currentUser);
        return userService.toUserResponse(saved);
    }
}
