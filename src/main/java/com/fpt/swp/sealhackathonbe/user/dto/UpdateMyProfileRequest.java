package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Cho phép user tự cập nhật thông tin cơ bản của mình (không đổi role/status).
 */
@Getter
@Setter
public class UpdateMyProfileRequest {

    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    /** Chỉ áp dụng cho External Student. */
    @Size(max = 200, message = "University name must not exceed 200 characters")
    private String universityName;
}
