package com.fpt.swp.sealhackathonbe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String roleName;
    private UUID teamId;
    private String teamName;
    private String teamStatus;
    private String accountStatus;
    private String accountStatusName;
    private String fptStudentCode;
    private String externalStudentCode;
    private String universityName;
    private LocalDateTime accountExpiresAt;
    private Boolean emailVerified;
    // Soft-deleted (isDeleted=1): hiển thị mờ + readonly trên trang quản lý.
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
