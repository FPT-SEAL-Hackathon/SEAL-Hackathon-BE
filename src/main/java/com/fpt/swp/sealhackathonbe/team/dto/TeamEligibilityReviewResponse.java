package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TeamEligibilityReviewResponse {
    private UUID teamId;
    private UUID eventId;
    private UUID categoryId;
    private String teamName;
    private UUID teamStatusId;
    private UUID leaderUserId;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Long activeMemberCount;
    private Boolean teamSizeEligible;
    private Boolean membersInfoComplete;
    private Boolean eligibleForCompetition;
    private List<String> issues;
    private List<TeamEligibilityMemberResponse> members;
}
