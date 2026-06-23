package com.fpt.swp.sealhackathonbe.research.dto;

import java.util.List;

public record ResearchDashboardResponse(
        List<VarianceReportResponse> varianceReport,
        List<ScoreDistributionResponse> scoreDistribution,
        List<ReliabilityMetricResponse> reliabilityMetrics
) {
}
