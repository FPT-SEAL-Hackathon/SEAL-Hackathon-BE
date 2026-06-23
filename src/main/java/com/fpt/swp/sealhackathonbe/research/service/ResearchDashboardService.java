package com.fpt.swp.sealhackathonbe.research.service;

import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ResearchDashboardResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ScoreDistributionResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ResearchDashboardService {
    ResearchDashboardResponse getDashboard(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize);

    List<VarianceReportResponse> getVarianceReport(UUID eventId, UUID roundId, UUID categoryId);

    List<ScoreDistributionResponse> getScoreDistribution(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize);

    List<ReliabilityMetricResponse> getReliabilityMetrics(UUID eventId, UUID roundId, UUID categoryId);
}
