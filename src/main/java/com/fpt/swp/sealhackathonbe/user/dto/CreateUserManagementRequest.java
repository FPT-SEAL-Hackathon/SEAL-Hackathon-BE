package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUserManagementRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters.")
    private String password;

    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    @Size(max = 50, message = "Account status must not exceed 50 characters")
    private String accountStatus;

    @Size(max = 20, message = "FPT student code must not exceed 20 characters")
    private String fptStudentCode;

    @Size(max = 50, message = "External student code must not exceed 50 characters")
    private String externalStudentCode;

    @Size(max = 200, message = "University name must not exceed 200 characters")
    private String universityName;

    private LocalDateTime accountExpiresAt;
}
