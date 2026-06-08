package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeamMemberResponse {
    private UUID teamMemberId;
    private UUID userId;
    private LocalDateTime joinedAt;
    private Boolean active;
}
