package com.fpt.swp.sealhackathonbe.judging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationAuditLogDTO {
    private UUID id;
    private UUID eventId;
    private String actionType;
    private UUID actorUserId;
    private UUID judgingId;
    private UUID teamId;
    private UUID submissionId;
    private String oldValue;
    private String newValue;
    private String reason;
    private LocalDateTime createdAt;
}
