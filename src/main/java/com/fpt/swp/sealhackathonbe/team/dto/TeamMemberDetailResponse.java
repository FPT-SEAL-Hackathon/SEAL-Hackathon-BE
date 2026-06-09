package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeamMemberDetailResponse {
    // DTO chi tiết thành viên: kết hợp dữ liệu membership trong TeamMembers và hồ sơ user trong Users.
    private UUID teamMemberId;
    private UUID teamId;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String fptStudentCode;
    private String externalStudentCode;
    private String universityName;
    private String userTypeName;
    private String accountStatusName;
    private LocalDateTime joinedAt;
    private Boolean active;
}
