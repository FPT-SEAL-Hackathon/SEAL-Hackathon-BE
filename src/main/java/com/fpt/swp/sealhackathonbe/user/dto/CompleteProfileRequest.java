package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Hoàn thiện hồ sơ cho user OAuth TEMPORARY (hoặc user thiếu hồ sơ).
 * Chỉ được tự chọn role FPT_STUDENT / EXTERNAL_STUDENT.
 */
@Getter
@Setter
public class CompleteProfileRequest {

    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "FPT student code must not exceed 20 characters")
    private String fptStudentCode;

    @Size(max = 50, message = "External student code must not exceed 50 characters")
    private String externalStudentCode;

    @Size(max = 200, message = "University name must not exceed 200 characters")
    private String universityName;
}
