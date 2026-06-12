package com.fpt.swp.sealhackathonbe.round.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateRoundRequest {
    private String roundName;
    private String description;
    private Integer roundOrder;
    private UUID roundStatusId;
    private LocalDateTime submissionDeadline;
    private LocalDateTime judgingDeadline;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer advancementTopN;
    private Boolean isCalibrationRound;
}
