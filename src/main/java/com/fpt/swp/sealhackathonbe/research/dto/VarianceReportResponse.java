package com.fpt.swp.sealhackathonbe.research.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VarianceReportResponse(
        UUID roundId,
        String roundName,
        UUID categoryId,
        String categoryName,
        UUID submissionId,
        UUID teamId,
        String teamName,
        UUID roundCriterionId,
        String criterionName,
        Long judgeCount,
        BigDecimal meanScore,
        BigDecimal standardDeviation,
        BigDecimal scoreRange,
        BigDecimal variance
) {
}
