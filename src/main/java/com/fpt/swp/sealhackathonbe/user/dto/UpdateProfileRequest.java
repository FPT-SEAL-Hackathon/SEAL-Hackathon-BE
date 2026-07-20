package com.fpt.swp.sealhackathonbe.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Schema(description = "Full name of the user", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Phone number of the user", example = "0987654321")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Schema(description = "University name (for External Student)", example = "FPT University")
    private String universityName;

    @Schema(description = "FPT Student code", example = "SE123456")
    private String fptStudentCode;

    @Schema(description = "External Student code", example = "20201234")
    private String externalStudentCode;
    
    @Schema(description = "User bio", example = "Passionate developer")
    private String bio;

    @Schema(description = "GitHub profile link", example = "https://github.com/abc")
    private String github;

    @Schema(description = "Portfolio link", example = "https://abc.com")
    private String portfolio;
}
