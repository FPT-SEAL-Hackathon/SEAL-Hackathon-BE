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
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String userType;
    private String roleName;
    private String userTypeName;
    private UUID teamId;
    private String teamName;
    private String teamStatus;
    private String status;
    private String accountStatus;
    private String statusName;
    private String accountStatusName;
    private String fptStudentCode;
    private String externalStudentCode;
    private String universityName;
    private LocalDateTime accountExpiresAt;
    private Boolean emailVerified;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
