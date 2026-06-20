package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TeamEligibilityMemberResponse {
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
    private List<String> issues;
}
