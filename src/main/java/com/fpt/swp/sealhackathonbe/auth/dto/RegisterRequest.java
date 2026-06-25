package com.fpt.swp.sealhackathonbe.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Dữ liệu đăng ký public dùng để tạo tài khoản student.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    private UUID userTypeId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100,
            message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Size(max = 20, message = "Student code must not exceed 20 characters")
    @NotBlank(message = "Student code is required")
    private String studentCode;

    @NotBlank(message = "University is required")
    @Size(max = 255, message = "University must not exceed 255 characters")
    private String universityName;;

    @NotBlank(message = "Phone number is required")
    @Size(max = 10, message = "Phone number must not exceed 10 characters")
    private String phone;

}
