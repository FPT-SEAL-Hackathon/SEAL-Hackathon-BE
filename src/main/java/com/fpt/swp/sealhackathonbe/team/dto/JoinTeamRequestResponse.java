package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JoinTeamRequestResponse {
    private UUID requestId;
    private UUID teamId;
    private UUID userId;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private UUID respondedById;
    private String responseNote;
}
