package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeamMemberResponse {
    // DTO thành viên trong TeamResponse; không expose teamId vì đã nằm trong response cha.
    private UUID teamMemberId;
    private UUID userId;
    private LocalDateTime joinedAt;
    private Boolean active;
}
