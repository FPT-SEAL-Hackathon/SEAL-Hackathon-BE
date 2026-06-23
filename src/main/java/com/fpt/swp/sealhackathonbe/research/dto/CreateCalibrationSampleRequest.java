package com.fpt.swp.sealhackathonbe.research.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCalibrationSampleRequest(
        @NotNull UUID roundId,
        @NotNull UUID submissionId,
        String referenceScoreJson
) {
}
