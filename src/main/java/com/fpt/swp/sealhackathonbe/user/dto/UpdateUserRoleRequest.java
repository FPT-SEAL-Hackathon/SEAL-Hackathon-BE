package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;

    @Size(max = 20, message = "FPT student code must not exceed 20 characters")
    private String fptStudentCode;

    @Size(max = 50, message = "External student code must not exceed 50 characters")
    private String externalStudentCode;

    @Size(max = 200, message = "University name must not exceed 200 characters")
    private String universityName;
}
