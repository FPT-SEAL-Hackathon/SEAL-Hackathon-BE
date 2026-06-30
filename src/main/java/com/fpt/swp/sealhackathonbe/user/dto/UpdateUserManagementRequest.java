package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateUserManagementRequest {
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    @Size(max = 20, message = "FPT student code must not exceed 20 characters")
    private String fptStudentCode;

    @Size(max = 50, message = "External student code must not exceed 50 characters")
    private String externalStudentCode;

    @Size(max = 200, message = "University name must not exceed 200 characters")
    private String universityName;

    private LocalDateTime accountExpiresAt;
}
