package com.fpt.swp.sealhackathonbe.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FptStudentCodePrefixRequest {

    @NotBlank(message = "Prefix is required")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Prefix must contain exactly 2 letters")
    private String prefix;

    @NotBlank(message = "English name is required")
    @Size(max = 100, message = "English name must not exceed 100 characters")
    private String englishName;

    @NotBlank(message = "Vietnamese name is required")
    @Size(max = 200, message = "Vietnamese name must not exceed 200 characters")
    private String vietnameseName;

    @NotBlank(message = "Major group is required")
    @Size(max = 100, message = "Major group must not exceed 100 characters")
    private String majorGroup;

    @Size(max = 20, message = "Major code must not exceed 20 characters")
    private String majorCode;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    private Boolean active;
}
