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
                        // SD/Variance = null khi bai chi co 1 giam khao cham (khong tinh duoc),
                        // khac han voi 0 = "cac giam khao cham y het nhau".
                        decimalOrNull(values[11]),
                        decimal(values[12]),
                        decimalOrNull(values[13])
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

    /**
     * Chi so do tin cay cua tung giam khao, tinh TREN DIEM HIEU CHUAN.
     *
     * Bo loc IsCalibration = 1 la bat buoc: day la so lieu cho man "Calibration Analytics".
     * Truoc day khong loc nen avg/bias/deviation gop ca diem bai THAT trong cung round —
     * dang dung mot cach tinh co vi FE chi cho chon calibration round, nhung chi can loc theo
     * event hoac category la so lieu lan ngay. Doi xung voi getVarianceReport (IsCalibration = 0).
     */
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
                      AND sc.IsCalibration = 1
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
                        // bias/AAD/RMS = null khi giam khao khong co dong nghiep nao cham cung
                        // bai (PeerMean IS NULL o moi dong). Truoc day ra 0 nen UI bao
                        // "do lech cua ban rat can bang" trong khi thuc te khong co gi de so.
                        decimalOrNull(values[8]),
                        decimalOrNull(values[9]),
                        decimalOrNull(values[10])
                ))
                .toList();
    }

    /**
     * Ma tran dong thuan cua vong hieu chuan, mot dong = mot (bai mau x tieu chi).
     *
     * @param viewerUserId    giam khao dang xem; null khi nguoi xem la Organizer/Admin
     * @param revealAllSamples true = Organizer/Admin, thay het; false = giam khao, chi thay bai
     *                         mau MA CHINH HO DA CHAM
     *
     * Vi sao phai chan: neu giam khao xem duoc median truoc khi cham, ho se cham theo con so do
     * (anchoring) va he thong ghi nhan mot su "dong thuan" gia tao — dung nguoc lai muc dich
     * cua vong hieu chuan.
     */
    public List<ConsensusMatrixResponse> getConsensusMatrix(UUID roundId, UUID viewerUserId, boolean revealAllSamples) {
        String sql = """
                SELECT
                    sc.SubmissionID,
                    rc.CriterionName,
                    rj.UserID AS JudgeUserID,
                    sc.ScoreValue,
                    rc.MaxScore,
                    s.SubmittedAt
                FROM Judging sc
                JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                WHERE s.RoundID = :roundId AND sc.IsCalibration = 1
                ORDER BY s.SubmittedAt, sc.SubmissionID, rc.SortOrder, rc.CriterionName
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("roundId", roundId)
                .getResultList();

        // Bai mau ma nguoi xem da cham -> quyet dinh duoc phep xem ket qua doi chieu hay chua.
        Set<UUID> scoredByViewer = new HashSet<>();
        if (viewerUserId != null) {
            for (Object[] row : rows) {
                if (viewerUserId.toString().equalsIgnoreCase(string(row[2]))) {
                    scoredByViewer.add(uuid(row[0]));
                }
            }
        }

        // Nhan "Sample 1..n" theo thu tu nop, on dinh giua cac lan goi (ORDER BY o tren).
        Map<UUID, String> sampleLabels = new LinkedHashMap<>();
        for (Object[] row : rows) {
            UUID submissionId = uuid(row[0]);
            if (!sampleLabels.containsKey(submissionId)) {
                sampleLabels.put(submissionId, "Sample " + (sampleLabels.size() + 1));
            }
        }

        // Gom theo CA submission LAN tieu chi. Truoc day chi gom theo tieu chi nen nhieu bai mau
        // bi tron diem vao nhau va judgeScores bi ghi de chi con bai cuoi.
        Map<String, List<Object[]>> grouped = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = string(row[0]) + "||" + string(row[1]);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        List<ConsensusMatrixResponse> result = new ArrayList<>();

        for (List<Object[]> groupRows : grouped.values()) {
            UUID submissionId = uuid(groupRows.get(0)[0]);
            if (!revealAllSamples && !scoredByViewer.contains(submissionId)) {
                continue; // chua cham bai mau nay -> chua duoc xem
            }

            String criteriaName = string(groupRows.get(0)[1]);
            BigDecimal maxPossible = decimal(groupRows.get(0)[4]);

            List<Double> scores = new ArrayList<>();
            List<BigDecimal> distribution = new ArrayList<>();
            Map<String, BigDecimal> judgeScores = new HashMap<>();

            for (Object[] row : groupRows) {
                // Mot giam khao chi co mot diem cho moi (bai, tieu chi) nen khong con ghi de.
                BigDecimal scoreValue = decimal(row[3]);
                scores.add(scoreValue.doubleValue());
                distribution.add(scoreValue);
                judgeScores.put(string(row[2]), scoreValue);
            }

            double median = StatisticsUtils.calculateMedian(new ArrayList<>(scores));
            double sd = StatisticsUtils.calculateSampleStandardDeviation(scores);
            double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);

            result.add(new ConsensusMatrixResponse(
                    submissionId,
                    sampleLabels.get(submissionId),
                    criteriaName,
                    BigDecimal.valueOf(median),
                    BigDecimal.valueOf(min),
                    BigDecimal.valueOf(max),
                    BigDecimal.valueOf(sd),
                    maxPossible,
                    // Chuan hoa theo thang diem cua chinh tieu chi: nguong tuyet doi cu khien
                    // tieu chi thang 100 luon DANGER con thang 5 thi luon GOOD.
                    StatisticsUtils.evaluateConsensusStatus(sd, maxPossible),
                    distribution,
                    judgeScores
            ));
        }
        return result;
    }

    /**
     * Xuat du lieu cham diem hieu chuan da AN DANH, dang DAI (long format):
     * mot dong = mot luot cham = (bai mau x tieu chi x giam khao).
     *
     * Vi sao khong dung dang rong (pivot) nhu truoc: de tinh do tin cay lien danh gia vien
     * (ICC, Krippendorff alpha) can ma tran DOI TUONG x NGUOI CHAM, trong do doi tuong la
     * tung BAI NOP. Ban cu lay tieu chi lam dong va lam mat chieu bai nop (nhieu bai mau bi
     * ghi de len nhau), nen khong chay duoc IRR. Dang dai nap thang vao R/Python roi pivot
     * lai tuy phan tich.
     *
     * Ma an danh gan theo thu tu UserID da sap xep nen ON DINH giua cac lan xuat (ban cu gan
     * theo thu tu duyet ket qua nen doi moi lan, khong ghep duoc 2 file), va dung so thu tu
     * co dem 0 thay vi ky tu A..Z (ban cu vuot qua 26 giam khao se sinh ky tu rac).
     */
    public String exportCalibrationCsv(UUID roundId) {
        String sql = """
                SELECT
                    sc.SubmissionID,
                    rc.CriterionName,
                    rj.UserID AS JudgeUserID,
                    sc.ScoreValue,
                    rc.MaxScore,
                    s.SubmittedAt
                FROM Judging sc
                JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
                JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
                JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
                WHERE s.RoundID = :roundId AND sc.IsCalibration = 1
                ORDER BY s.SubmittedAt, sc.SubmissionID, rc.SortOrder, rc.CriterionName, rj.UserID
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("roundId", roundId)
                .getResultList();

        // Ma an danh on dinh: sap xep UserID roi danh so.
        List<String> sortedJudgeIds = rows.stream()
                .map(row -> string(row[2]))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        Map<String, String> judgeAlias = new HashMap<>();
        for (int i = 0; i < sortedJudgeIds.size(); i++) {
            judgeAlias.put(sortedJudgeIds.get(i), String.format("Judge_%02d", i + 1));
        }

        // Nhan bai mau theo thu tu nop, khop voi nhan tren man hinh Consensus Matrix.
        Map<UUID, String> sampleLabels = new LinkedHashMap<>();
        for (Object[] row : rows) {
            UUID submissionId = uuid(row[0]);
            if (!sampleLabels.containsKey(submissionId)) {
                sampleLabels.put(submissionId, "Sample " + (sampleLabels.size() + 1));
            }
        }

        StringBuilder csv = new StringBuilder();
        // LUU Y: phai la ky tu xuong dong that. Ban cu viet "\\n" trong string thuong nen
        // xuat ra ky tu '\' + 'n' va toan bo file bi dinh thanh MOT dong.
        csv.append("round_id,submission_id,sample_label,criterion,max_score,judge_anon,score\n");

        for (Object[] row : rows) {
            UUID submissionId = uuid(row[0]);
            csv.append(roundId).append(',')
                    .append(submissionId).append(',')
                    .append(csvQuote(sampleLabels.get(submissionId))).append(',')
                    .append(csvQuote(string(row[1]))).append(',')
                    .append(row[4] == null ? "" : decimal(row[4]).toPlainString()).append(',')
                    .append(judgeAlias.getOrDefault(string(row[2]), "Judge_00")).append(',')
                    .append(row[3] == null ? "" : decimal(row[3]).toPlainString())
                    .append('\n');
        }

        return csv.toString();
    }

    /** Boc chuoi trong dau nhay kep va nhan doi dau nhay ben trong theo dung RFC 4180. */
    private String csvQuote(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
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

    /**
     * Danh cho cac cot THONG KE (do lech chuan, phuong sai, bias...): giu nguyen null thay vi
     * quy ve 0. SQL Server tra NULL khi khong du du lieu de tinh — vi du STDEV/VAR tren mot
     * dong duy nhat (bai chi co 1 giam khao cham), hoac AVG cua tap rong (giam khao khong co
     * dong nghiep nao cham cung bai). Quy ve 0 bien "khong tinh duoc" thanh "lech bang 0",
     * tuc la bao cao mot su dong thuan hoan hao khong he ton tai.
     */
    private BigDecimal decimalOrNull(Object value) {
        return value == null ? null : decimal(value);
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
