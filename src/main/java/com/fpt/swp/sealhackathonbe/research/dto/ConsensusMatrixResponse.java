package com.fpt.swp.sealhackathonbe.research.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ConsensusMatrixResponse(
        String criteriaName,
        BigDecimal median,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal standardDeviation,
        String status,
        Map<String, BigDecimal> judgeScores // Map of Judge UserID to their score for this criteria
) {
}
