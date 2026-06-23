package com.fpt.swp.sealhackathonbe.research.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CalibrationSampleResponse(
        UUID sampleId,
        UUID roundId,
        String roundName,
        UUID submissionId,
        UUID teamId,
        String teamName,
        String referenceScoreJson,
        UUID addedById,
        String addedByName,
        LocalDateTime addedAt
) {
}
