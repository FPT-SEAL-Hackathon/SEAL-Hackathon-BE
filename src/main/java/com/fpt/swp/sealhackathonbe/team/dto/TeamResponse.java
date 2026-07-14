package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TeamResponse {

    // DTO tổng hợp thông tin team và danh sách member active để trả về API.
    private UUID teamId;
    private UUID eventId;
    private UUID categoryId;
    private String teamName;
    private UUID teamStatusId;
    private String teamStatusName;
    private UUID leaderUserId;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Long activeMemberCount;
    private Boolean teamSizeEligible;
    private Boolean membersInfoComplete;
    private Boolean canRequestApproval;
    private List<String> approvalIssues;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TeamMemberResponse> members;
}
