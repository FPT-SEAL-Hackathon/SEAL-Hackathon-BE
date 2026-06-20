package com.fpt.swp.sealhackathonbe.judging.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScoreSubmissionDTO {

    @NotNull(message = "Submission ID is required")
    private UUID submissionId;

    @NotNull(message = "Event Criterion ID is required")
    private UUID roundCriterionId;

    @NotNull(message = "Score value is required")
    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0")
    private BigDecimal scoreValue;

    private String comment;

    private Boolean isCalibration;

}