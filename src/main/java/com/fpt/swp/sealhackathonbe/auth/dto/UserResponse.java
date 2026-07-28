package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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

    private String bio;
    private String github;
    private String portfolio;

    private LocalDateTime createdAt;

    // Chuẩn hóa hồ sơ: profileCompliant = true nếu hồ sơ đã đúng định dạng chuẩn;
    // profileIssues liệt kê các lỗi để FE hiện banner nhắc cập nhật. Không chặn dùng.
    private Boolean profileCompliant;
    private List<String> profileIssues;
}
