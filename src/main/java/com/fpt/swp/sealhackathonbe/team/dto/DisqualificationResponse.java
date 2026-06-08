package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisqualificationResponse {
    private UUID disqualificationId;
    private UUID teamId;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
    private Boolean reversed;
}
