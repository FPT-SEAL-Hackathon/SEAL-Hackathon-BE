package com.fpt.swp.sealhackathonbe.round.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RoundResponse {
    private UUID roundId;
    private UUID categoryId;
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
