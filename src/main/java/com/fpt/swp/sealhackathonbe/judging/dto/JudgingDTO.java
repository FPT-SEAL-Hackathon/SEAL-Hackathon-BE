package com.fpt.swp.sealhackathonbe.judging.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JudgingDTO {
    private UUID id;
    private UUID submissionId;
    private UUID roundJudgeId;
    private String judgeName;
    private UUID roundCriterionId;
    private String criterionName;
    private BigDecimal scoreValue;
    private String comment;
    private LocalDateTime scoredAt;
    private LocalDateTime updatedAt;
    private Boolean isCalibration;
}
