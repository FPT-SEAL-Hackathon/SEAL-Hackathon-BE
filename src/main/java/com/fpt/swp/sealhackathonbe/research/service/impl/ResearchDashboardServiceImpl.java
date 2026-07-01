package com.fpt.swp.sealhackathonbe.research.service.impl;

import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ScoreDistributionResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResearchDashboardServiceImpl {
    private static final BigDecimal DEFAULT_BUCKET_SIZE = BigDecimal.TEN;

    private final EntityManager entityManager;


    public List<VarianceReportResponse> getVarianceReport(UUID eventId, UUID roundId, UUID categoryId) {
        String sql = applyFilters("""
                SELECT
                    s.RoundID,
                    r.RoundName,
                    t.CategoryID,
                    c.CategoryName,
                    sc.SubmissionID,
                    t.TeamID,
                    t.TeamName,
                    rc.RoundCriterionID,
                    rc.CriterionName,
                    COUNT(DISTINCT rj.UserID) AS JudgeCount,
                    AVG(sc.ScoreValue) AS MeanScore,
                    STDEV(sc.ScoreValue) AS StdDevScore,
                    MAX(sc.ScoreValue) - MIN(sc.ScoreValue) AS ScoreRange,
                    VAR(sc.ScoreValue) AS VarianceScore
                FROM Judging sc
                         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                         JOIN Teams t ON t.TeamID = s.TeamID
                         JOIN Categories c ON c.CategoryID = t.CategoryID
                         JOIN Rounds r ON r.RoundID = s.RoundID
                         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                         JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                WHERE (:eventId IS NULL OR t.EventID = :eventId)
                  AND sc.IsCalibration = 0
                """, roundId, categoryId) + """
                GROUP BY s.RoundID, r.RoundName, t.CategoryID, c.CategoryName, sc.SubmissionID,
                         t.TeamID, t.TeamName, rc.RoundCriterionID, rc.CriterionName
                ORDER BY r.RoundName, t.TeamName, rc.CriterionName
                """;

        return query(sql, eventId, roundId, categoryId).getResultList().stream()
                .map(row -> {
                    Object[] values = (Object[]) row;
                    return new VarianceReportResponse(
                            uuid(values[0]),
                            string(values[1]),
                            uuid(values[2]),
                            string(values[3]),
                            uuid(values[4]),
                            uuid(values[5]),
                            string(values[6]),
                            uuid(values[7]),
                            string(values[8]),
                            longValue(values[9]),
                            decimal(values[10]),
                            decimal(values[11]),
                            decimal(values[12]),
                            decimal(values[13])
                    );
                })
                .toList();
    }

    public List<ScoreDistributionResponse> getScoreDistribution(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        BigDecimal normalizedBucketSize = normalizeBucketSize(bucketSize);
        String innerSql = applyFilters("""
                SELECT
                    FLOOR(sc.ScoreValue / :bucketSize) * :bucketSize AS BucketStart
                FROM Judging sc
                         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                         JOIN Teams t ON t.TeamID = s.TeamID
                WHERE (:eventId IS NULL OR t.EventID = :eventId)
                  AND sc.IsCalibration = 0
                """, roundId, categoryId);
        String sql = "SELECT src.BucketStart, COUNT(*) AS ScoreCount FROM (" + innerSql + ") src GROUP BY src.BucketStart ORDER BY src.BucketStart";

        List<Object[]> rows = query(sql, eventId, roundId, categoryId)
                .setParameter("bucketSize", normalizedBucketSize)
                .getResultList();
        long total = rows.stream()
                .mapToLong(row -> longValue(row[1]))
                .sum();

        return rows.stream()
                .map(row -> {
                    BigDecimal bucketStart = decimal(row[0]);
                    long scoreCount = longValue(row[1]);
                    BigDecimal percentage = total == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(scoreCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
                    return new ScoreDistributionResponse(
                            bucketStart,
                            bucketStart.add(normalizedBucketSize),
                            scoreCount,
                            percentage
                    );
                })
                .toList();
    }

    public List<ReliabilityMetricResponse> getReliabilityMetrics(UUID eventId, UUID roundId, UUID categoryId) {
        String sql = applyFilters("""
                WITH ScoreComparisons AS (
                    SELECT
                        rj.UserID AS JudgeUserID,
                        u.FullName AS JudgeName,
                        sc.ScoreValue,
                        sc.IsCalibration,
                        (
                            SELECT AVG(peer.ScoreValue)
                            FROM Judging peer
                                     JOIN RoundJudges peerJudge ON peerJudge.RoundJudgeID = peer.RoundJudgeID
                            WHERE peer.SubmissionID = sc.SubmissionID
                              AND peer.RoundCriterionID = sc.RoundCriterionID
                              AND peer.IsCalibration = 0
                              AND peerJudge.UserID <> rj.UserID
                        ) AS PeerMean
                    FROM Judging sc
                             JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                             JOIN Teams t ON t.TeamID = s.TeamID
                             JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                             JOIN Users u ON u.UserID = rj.UserID
                    WHERE (:eventId IS NULL OR t.EventID = :eventId)
                """, roundId, categoryId) + """
                )
                SELECT
                    JudgeUserID,
                    JudgeName,
                    COUNT(*) AS ScoredItemCount,
                    SUM(CASE WHEN IsCalibration = 0 AND PeerMean IS NOT NULL THEN 1 ELSE 0 END) AS ComparableScoreCount,
                    SUM(CASE WHEN IsCalibration = 1 THEN 1 ELSE 0 END) AS CalibrationScoreCount,
                    AVG(ScoreValue) AS AverageScore,
                    MIN(ScoreValue) AS MinScore,
                    MAX(ScoreValue) AS MaxScore,
                    AVG(CASE WHEN IsCalibration = 0 AND PeerMean IS NOT NULL THEN ScoreValue - PeerMean END) AS BiasFromPeerMean,
                    AVG(CASE WHEN IsCalibration = 0 AND PeerMean IS NOT NULL THEN ABS(ScoreValue - PeerMean) END) AS AvgAbsDeviation,
                    SQRT(AVG(CASE WHEN IsCalibration = 0 AND PeerMean IS NOT NULL THEN POWER(ScoreValue - PeerMean, 2) END)) AS RootMeanSquareDeviation
                FROM ScoreComparisons
                GROUP BY JudgeUserID, JudgeName
                ORDER BY AvgAbsDeviation ASC, JudgeName ASC
                """;

        return query(sql, eventId, roundId, categoryId).getResultList().stream()
                .map(row -> {
                    Object[] values = (Object[]) row;
                    return new ReliabilityMetricResponse(
                            uuid(values[0]),
                            string(values[1]),
                            longValue(values[2]),
                            longValue(values[3]),
                            longValue(values[4]),
                            decimal(values[5]),
                            decimal(values[6]),
                            decimal(values[7]),
                            decimal(values[8]),
                            decimal(values[9]),
                            decimal(values[10])
                    );
                })
                .toList();
    }

    private Query query(String sql, UUID eventId, UUID roundId, UUID categoryId) {
        Query query = entityManager.createNativeQuery(sql);
        
        // Always bind eventId to handle :eventId IS NULL OR ...
        query.setParameter("eventId", eventId);
        if (roundId != null) {
            query.setParameter("roundId", roundId);
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        return query;
    }

    private String applyFilters(String sql, UUID roundId, UUID categoryId) {
        StringBuilder builder = new StringBuilder(sql);
        if (roundId != null) {
            builder.append(" AND s.RoundID = :roundId\n");
        }
        if (categoryId != null) {
            builder.append(" AND t.CategoryID = :categoryId\n");
        }
        return builder.toString();
    }

    private BigDecimal normalizeBucketSize(BigDecimal bucketSize) {
        if (bucketSize == null || bucketSize.compareTo(BigDecimal.ZERO) <= 0) {
            return DEFAULT_BUCKET_SIZE;
        }
        return bucketSize;
    }

    private UUID uuid(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }

    private String string(Object value) {
        return value == null ? null : value.toString();
    }

    private Long longValue(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
