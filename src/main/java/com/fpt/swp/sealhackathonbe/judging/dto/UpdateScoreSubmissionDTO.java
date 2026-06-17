package com.fpt.swp.sealhackathonbe.judging.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateScoreSubmissionDTO {

    @NotNull(message = "Judging ID is required")
    private java.util.UUID judgingId;

    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0")
    private BigDecimal scoreValue;

    private String comment;

    private Boolean isCalibration;

    @NotBlank(message = "Reason for this change is mandatory")
    private String reason;
}
