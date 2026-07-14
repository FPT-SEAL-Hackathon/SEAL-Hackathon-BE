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
import java.util.*;
import java.util.stream.Collectors;
import com.fpt.swp.sealhackathonbe.core.utils.StatisticsUtils;
import com.fpt.swp.sealhackathonbe.research.dto.ConsensusMatrixResponse;

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
                    c.CategoryID,
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
                         LEFT JOIN Teams t ON t.TeamID = s.TeamID
                         JOIN Rounds r ON r.RoundID = s.RoundID
                         JOIN Categories c ON c.CategoryID = r.CategoryID
                         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                         JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                WHERE (:eventId IS NULL OR c.EventID = :eventId)
                  AND sc.IsCalibration = 0
                """, roundId, categoryId) + """
                GROUP BY s.RoundID, r.RoundName, c.CategoryID, c.CategoryName, sc.SubmissionID,
                         t.TeamID, t.TeamName, rc.RoundCriterionID, rc.CriterionName
                ORDER BY r.RoundName, t.TeamName, rc.CriterionName
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query(sql, eventId, roundId, categoryId).getResultList();

        return rows.stream()
                .map(values -> new VarianceReportResponse(
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
                ))
                .toList();
    }

    public List<ScoreDistributionResponse> getScoreDistribution(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        BigDecimal normalizedBucketSize = normalizeBucketSize(bucketSize);
        String baseSql = applyFilters("""
                SELECT
                    rj.UserID AS JudgeUserID,
                    sc.SubmissionID,
                    SUM(sc.ScoreValue) AS TotalScore
                FROM Judging sc
                         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                         LEFT JOIN Teams t ON t.TeamID = s.TeamID
                         JOIN Rounds r ON r.RoundID = s.RoundID
                         JOIN Categories c ON c.CategoryID = r.CategoryID
                         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                WHERE (:eventId IS NULL OR c.EventID = :eventId)
                  AND sc.IsCalibration = 0
                """, roundId, categoryId);
        String innerSql = "SELECT FLOOR(st.TotalScore / :bucketSize) * :bucketSize AS BucketStart FROM (" + baseSql + " GROUP BY rj.UserID, sc.SubmissionID) st";
        String sql = "SELECT src.BucketStart, COUNT(*) AS ScoreCount FROM (" + innerSql + ") src GROUP BY src.BucketStart ORDER BY src.BucketStart";

        @SuppressWarnings("unchecked")
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
        String sql = "WITH JudgeSubmissionScores AS (" + applyFilters("""
                    SELECT
                        rj.UserID AS JudgeUserID,
                        u.FullName AS JudgeName,
                        sc.SubmissionID,
                        sc.IsCalibration,
                        SUM(sc.ScoreValue) AS TotalScore
                    FROM Judging sc
                             JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                             LEFT JOIN Teams t ON t.TeamID = s.TeamID
                             JOIN Rounds r ON r.RoundID = s.RoundID
                             JOIN Categories c ON c.CategoryID = r.CategoryID
                             JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                             JOIN Users u ON u.UserID = rj.UserID
                    WHERE (:eventId IS NULL OR c.EventID = :eventId)
                """, roundId, categoryId) + """
                    GROUP BY rj.UserID, u.FullName, sc.SubmissionID, sc.IsCalibration
                ),
                ScoreComparisons AS (
                    SELECT
                        jss.JudgeUserID,
                        jss.JudgeName,
                        jss.TotalScore,
                        jss.IsCalibration,
                        (
                            SELECT AVG(peer_total)
                            FROM (
                                SELECT rjp.UserID AS PeerUserID, SUM(scp.ScoreValue) AS peer_total
                                FROM Judging scp
                                JOIN RoundJudges rjp ON rjp.RoundJudgeID = scp.RoundJudgeID
                                WHERE scp.SubmissionID = jss.SubmissionID
                                  AND scp.IsCalibration = jss.IsCalibration
                                  AND rjp.UserID <> jss.JudgeUserID
                                GROUP BY rjp.UserID
                            ) peer_aggs
                        ) AS PeerMean
                    FROM JudgeSubmissionScores jss
                )
                SELECT
                    JudgeUserID,
                    JudgeName,
                    COUNT(*) AS ScoredItemCount,
                    SUM(CASE WHEN PeerMean IS NOT NULL THEN 1 ELSE 0 END) AS ComparableScoreCount,
                    SUM(CASE WHEN IsCalibration = 1 THEN 1 ELSE 0 END) AS CalibrationScoreCount,
                    AVG(TotalScore) AS AverageScore,
                    MIN(TotalScore) AS MinScore,
                    MAX(TotalScore) AS MaxScore,
                    AVG(CASE WHEN PeerMean IS NOT NULL THEN TotalScore - PeerMean END) AS BiasFromPeerMean,
                    AVG(CASE WHEN PeerMean IS NOT NULL THEN ABS(TotalScore - PeerMean) END) AS AvgAbsDeviation,
                    SQRT(AVG(CASE WHEN PeerMean IS NOT NULL THEN POWER(TotalScore - PeerMean, 2) END)) AS RootMeanSquareDeviation
                FROM ScoreComparisons
                GROUP BY JudgeUserID, JudgeName
                ORDER BY AvgAbsDeviation ASC, JudgeName ASC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query(sql, eventId, roundId, categoryId).getResultList();

        return rows.stream()
                .map(values -> new ReliabilityMetricResponse(
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
                ))
                .toList();
    }

    public List<ConsensusMatrixResponse> getConsensusMatrix(UUID roundId) {
        String sql = """
                SELECT
                    rc.CriterionName,
                    rj.UserID AS JudgeUserID,
                    sc.ScoreValue
                FROM Judging sc
                JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                WHERE s.RoundID = :roundId AND sc.IsCalibration = 1
                """;
        
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("roundId", roundId)
                .getResultList();
        
        // Group by criteria name
        Map<String, List<Object[]>> groupedByCriteria = rows.stream()
                .collect(Collectors.groupingBy(row -> string(row[0])));
                
        List<ConsensusMatrixResponse> result = new ArrayList<>();
        
        for (Map.Entry<String, List<Object[]>> entry : groupedByCriteria.entrySet()) {
            String criteriaName = entry.getKey();
            List<Object[]> criteriaRows = entry.getValue();
            
            List<Double> scores = new ArrayList<>();
            Map<String, BigDecimal> judgeScores = new HashMap<>();
            
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            
            for (Object[] row : criteriaRows) {
                String judgeId = string(row[1]);
                BigDecimal scoreValue = decimal(row[2]);
                double score = scoreValue.doubleValue();
                
                scores.add(score);
                judgeScores.put(judgeId, scoreValue);
                
                if (score < min) min = score;
                if (score > max) max = score;
            }
            
            if (min == Double.MAX_VALUE) min = 0;
            if (max == Double.MIN_VALUE) max = 0;
            
            double median = StatisticsUtils.calculateMedian(scores);
            double sd = StatisticsUtils.calculateSampleStandardDeviation(scores);
            String status = StatisticsUtils.evaluateConsensusStatus(sd);
            
            result.add(new ConsensusMatrixResponse(
                    criteriaName,
                    BigDecimal.valueOf(median),
                    BigDecimal.valueOf(min),
                    BigDecimal.valueOf(max),
                    BigDecimal.valueOf(sd),
                    status,
                    judgeScores
            ));
        }
        return result;
    }

    public String exportCalibrationCsv(UUID roundId) {
        String sql = """
                SELECT
                    rc.CriterionName,
                    rj.UserID AS JudgeUserID,
                    sc.ScoreValue
                FROM Judging sc
                JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                WHERE s.RoundID = :roundId AND sc.IsCalibration = 1
                ORDER BY rc.CriterionName, rj.UserID
                """;
                
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("roundId", roundId)
                .getResultList();
                
        // Extract unique judges
        Set<String> judgeIds = new LinkedHashSet<>();
        for (Object[] row : rows) {
            judgeIds.add(string(row[1]));
        }
        
        // Map JudgeId to Anonymized Name
        Map<String, String> judgeAnonymizedMap = new HashMap<>();
        char judgeChar = 'A';
        for (String judgeId : judgeIds) {
            judgeAnonymizedMap.put(judgeId, "Judge_" + judgeChar);
            judgeChar++;
        }
        
        // Group by criteria
        Map<String, Map<String, BigDecimal>> criteriaScores = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String criteriaName = string(row[0]);
            String judgeId = string(row[1]);
            BigDecimal score = decimal(row[2]);
            
            criteriaScores.putIfAbsent(criteriaName, new HashMap<>());
            criteriaScores.get(criteriaName).put(judgeId, score);
        }
        
        // Build CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Submission_ID,Criteria_Name");
        for (String judgeId : judgeIds) {
            csv.append(",").append(judgeAnonymizedMap.get(judgeId));
        }
        csv.append("\\n");
        
        for (Map.Entry<String, Map<String, BigDecimal>> entry : criteriaScores.entrySet()) {
            csv.append(roundId.toString()).append(",");
            csv.append("\\\"").append(entry.getKey()).append("\\\"");
            
            Map<String, BigDecimal> scores = entry.getValue();
            for (String judgeId : judgeIds) {
                BigDecimal score = scores.get(judgeId);
                csv.append(",");
                if (score != null) {
                    csv.append(score.toString());
                }
            }
            csv.append("\\n");
        }
        
        return csv.toString();
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
            builder.append(" AND c.CategoryID = :categoryId\n");
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
