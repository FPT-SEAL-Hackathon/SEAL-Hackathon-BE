package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeamWithdrawalRequestResponse {
    private UUID requestId;
    private UUID teamId;
    private String teamName;
    private UUID eventId;
    private UUID requestedById;
    private String requestedByName;
    private String requestedByEmail;
    private String reason;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private UUID respondedById;
    private String responseNote;
}
