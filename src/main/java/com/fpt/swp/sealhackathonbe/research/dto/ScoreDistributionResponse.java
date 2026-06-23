package com.fpt.swp.sealhackathonbe.research.dto;

import java.math.BigDecimal;

public record ScoreDistributionResponse(
        BigDecimal bucketStart,
        BigDecimal bucketEnd,
        Long scoreCount,
        BigDecimal percentage
) {
}
