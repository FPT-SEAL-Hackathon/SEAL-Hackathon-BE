package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thông tin hồ sơ an toàn được trả về từ API xác thực.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID userId;

    private String email;

    private String fullName;

    private String phone;

    private String role;
    private String roleName;

    private String accountStatus;
    private String accountStatusName;

    private String fptStudentCode;
    private String externalStudentCode;

    private String universityName;

    private LocalDateTime createdAt;
}
