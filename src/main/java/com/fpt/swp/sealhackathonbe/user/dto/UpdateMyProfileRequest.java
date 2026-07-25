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

    // Học sinh tự sửa mã SV của mình để chuẩn hóa (SE/SS/SA + 6 số cho FPT).
    // Chỉ nhận đúng loại theo role; validate + check trùng ở controller.
    @Size(max = 20, message = "FPT student code must not exceed 20 characters")
    private String fptStudentCode;

    @Size(max = 50, message = "External student code must not exceed 50 characters")
    private String externalStudentCode;
}
