package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisqualifiedTeamResponse {
    private UUID disqualificationId;
    private TeamResponse team;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
}
