package com.fpt.swp.sealhackathonbe.round.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateRoundRequest {
    private String roundName;
    private String description;
    private Integer roundOrder;
    private UUID roundStatusId;
    private LocalDateTime submissionDeadline;
    private LocalDateTime judgingDeadline;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime appealStartTime;
    private LocalDateTime appealEndTime;
    private Integer advancementTopN;
    private Boolean isCalibrationRound;
}
