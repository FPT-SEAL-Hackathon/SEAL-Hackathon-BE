package com.fpt.swp.sealhackathonbe.research.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReliabilityMetricResponse(
        UUID judgeUserId,
        String judgeName,
        Long scoredItemCount,
        Long comparableScoreCount,
        Long calibrationScoreCount,
        BigDecimal averageScore,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal biasFromPeerMean,
        BigDecimal averageAbsoluteDeviation,
        BigDecimal rootMeanSquareDeviation
) {
}
