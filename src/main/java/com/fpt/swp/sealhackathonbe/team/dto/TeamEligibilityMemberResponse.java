package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TeamEligibilityMemberResponse {
    // Chi tiet tung member trong man organizer review eligibility cua team.
    private UUID teamMemberId;
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
    private Boolean profileComplete;
    // Danh sach ly do member chua du dieu kien, vi du thieu phone/student code.
    private List<String> issues;
}
