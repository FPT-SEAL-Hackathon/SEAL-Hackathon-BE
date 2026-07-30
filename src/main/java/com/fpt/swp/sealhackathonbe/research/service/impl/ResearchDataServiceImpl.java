package com.fpt.swp.sealhackathonbe.research.service.impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ScoreDistributionResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;
import com.fpt.swp.sealhackathonbe.research.entity.DataExportLog;
import com.fpt.swp.sealhackathonbe.research.repository.DataExportLogRepository;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDataService;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ResearchDataServiceImpl implements ResearchDataService {
    private static final String CSV_CONTENT_TYPE = "text/csv; charset=UTF-8";

    private final DataExportLogRepository dataExportLogRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ResearchDashboardServiceImpl researchDashboardService;

    @Override
    @Transactional
    public DownloadFileResponse exportResearchData(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize, String type, UUID currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User exportedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String normalizedType = normalizeType(type);
        byte[] content;
        String filename;
        String contentType;
        int rowCount = 0;

        if ("dashboard".equals(normalizedType)) {
            content = buildDashboardZip(eventId, roundId, categoryId, bucketSize);
            filename = "research-dashboard-" + eventId + ".zip";
            contentType = "application/zip";
            rowCount = 1; // Generic count for zip
        } else {
            ExportContent exportContent = buildExportContent(eventId, roundId, categoryId, bucketSize, type);
            content = writeCsv(exportContent.header(), exportContent.rows());
            filename = "research-" + normalizedType + "-" + eventId + ".csv";
            contentType = CSV_CONTENT_TYPE;
            rowCount = exportContent.rows().size();
        }

        DataExportLog log = new DataExportLog();
        log.setEvent(event);
        log.setExportedBy(exportedBy);
        log.setFileFormat(contentType.equals("application/zip") ? "ZIP" : "CSV");
        log.setRowCount(rowCount);
        log.setNotes("Research export: " + normalizedType);
        dataExportLogRepository.save(log);

        return new DownloadFileResponse(filename, contentType, content);
    }

    private ExportContent buildExportContent(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize, String type) {
        return switch (normalizeType(type)) {
            case "variance-report" -> buildVarianceExport(eventId, roundId, categoryId, false);
            case "score-distribution" -> buildDistributionExport(eventId, roundId, categoryId, bucketSize);
            case "reliability-metrics" -> buildReliabilityExport(eventId, roundId, categoryId, false);
            default -> throw new IllegalArgumentException("Unsupported research export type: " + type);
        };
    }

    private String getPseudoId(Map<UUID, String> map, UUID id, String prefix) {
        if (id == null) return "";
        return map.computeIfAbsent(id, k -> prefix + "_" + (map.size() + 1));
    }

    private byte[] buildDashboardZip(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1. Variance Report (Anonymized)
            ExportContent variance = buildVarianceExport(eventId, roundId, categoryId, true);
            zos.putNextEntry(new ZipEntry("variance-report.csv"));
            zos.write(writeCsv(variance.header(), variance.rows()));
            zos.closeEntry();

            // 2. Score Distribution
            ExportContent distribution = buildDistributionExport(eventId, roundId, categoryId, bucketSize);
            zos.putNextEntry(new ZipEntry("score-distribution.csv"));
            zos.write(writeCsv(distribution.header(), distribution.rows()));
            zos.closeEntry();

            // 3. Reliability Metrics (Anonymized)
            ExportContent reliability = buildReliabilityExport(eventId, roundId, categoryId, true);
            zos.putNextEntry(new ZipEntry("reliability-metrics.csv"));
            zos.write(writeCsv(reliability.header(), reliability.rows()));
            zos.closeEntry();

            // 4. Data Dictionary
            zos.putNextEntry(new ZipEntry("data-dictionary.txt"));
            zos.write(buildDataDictionary().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate research dashboard ZIP", e);
        }
    }

    private String buildDataDictionary() {
        return "NOTES & DEFINITIONS\n\n" +
               "Data Anonymization:\n" +
               "All identifying UUIDs and names have been replaced with pseudo-IDs (e.g., TEAM_1, JUDGE_1) for research purposes.\n" +
               "Fields like CategoryName, RoundName, TeamName, CriterionName, JudgeName will be empty.\n\n" +
               "variance-report.csv\n" +
               "Analyzes the spread of scores for a single submission across different judges.\n" +
               "- JudgeCount: Number of judges who scored this criterion for this submission.\n" +
               "- MeanScore: Average score given by all judges for this criterion.\n" +
               "- StandardDeviation: Standard deviation indicating how spread out the scores are.\n" +
               "- ScoreRange: Difference between highest and lowest score.\n" +
               "- Variance: The variance of the scores, indicating judge agreement.\n\n" +
               "score-distribution.csv\n" +
               "Shows how many scores fall into specific score buckets.\n" +
               "- ScoreCount: Number of scores in this bucket.\n" +
               "- MinScore: Start of the score bucket.\n" +
               "- MaxScore: End of the score bucket.\n" +
               "- Percentage: Percentage of total scores in this bucket.\n\n" +
               "reliability-metrics.csv\n" +
               "Evaluates individual judge performance compared to their peers.\n" +
               "- ScoredItemCount: Number of items scored by this judge.\n" +
               "- AverageScore: The judge's average score across all their gradings.\n" +
               "- Bias: How much higher/lower this judge scores compared to the average of other judges.\n" +
               "- MeanAbsoluteDeviation: Average Absolute Deviation from the peer mean.\n" +
               "- RMSD: Root Mean Square Deviation from the peer mean (penalizes large outliers more).\n";
    }

    private ExportContent buildVarianceExport(UUID eventId, UUID roundId, UUID categoryId, boolean isAnonymized) {
        Map<UUID, String> roundMap = new HashMap<>();
        Map<UUID, String> categoryMap = new HashMap<>();
        Map<UUID, String> submissionMap = new HashMap<>();
        Map<UUID, String> teamMap = new HashMap<>();
        Map<UUID, String> criterionMap = new HashMap<>();

        List<String[]> rows = researchDashboardService.getVarianceReport(eventId, roundId, categoryId).stream()
                .map(item -> new String[]{
                        isAnonymized ? getPseudoId(categoryMap, item.categoryId(), "CATEGORY") : valueOf(item.categoryId()),
                        isAnonymized ? "" : valueOf(item.categoryName()),
                        isAnonymized ? getPseudoId(roundMap, item.roundId(), "ROUND") : valueOf(item.roundId()),
                        isAnonymized ? "" : valueOf(item.roundName()),
                        isAnonymized ? getPseudoId(submissionMap, item.submissionId(), "SUBMISSION") : valueOf(item.submissionId()),
                        isAnonymized ? getPseudoId(teamMap, item.teamId(), "TEAM") : valueOf(item.teamId()),
                        isAnonymized ? "" : valueOf(item.teamName()),
                        isAnonymized ? getPseudoId(criterionMap, item.roundCriterionId(), "CRITERION") : valueOf(item.roundCriterionId()),
                        isAnonymized ? "" : valueOf(item.criterionName()),
                        valueOf(item.judgeCount()),
                        valueOf(item.meanScore()),
                        valueOf(item.standardDeviation()),
                        valueOf(item.scoreRange()),
                        valueOf(item.variance())
                })
                .toList();
        return new ExportContent(
                new String[]{"CategoryID", "CategoryName", "RoundID", "RoundName", "SubmissionID", "TeamID", "TeamName", "RoundCriterionID", "CriterionName", "JudgeCount", "MeanScore", "StandardDeviation", "ScoreRange", "Variance"},
                rows
        );
    }

    private ExportContent buildDistributionExport(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        List<String[]> rows = researchDashboardService.getScoreDistribution(eventId, roundId, categoryId, bucketSize).stream()
                .map(item -> new String[]{
                        valueOf(item.bucketStart()),
                        valueOf(item.bucketEnd()),
                        valueOf(item.scoreCount()),
                        valueOf(item.percentage())
                })
                .toList();
        return new ExportContent(
                new String[]{"MinScore", "MaxScore", "ScoreCount", "Percentage"},
                rows
        );
    }

    private ExportContent buildReliabilityExport(UUID eventId, UUID roundId, UUID categoryId, boolean isAnonymized) {
        Map<UUID, String> judgeMap = new HashMap<>();
        List<String[]> rows = researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId).stream()
                .map(item -> new String[]{
                        isAnonymized ? getPseudoId(judgeMap, item.judgeUserId(), "JUDGE") : valueOf(item.judgeUserId()),
                        isAnonymized ? "" : valueOf(item.judgeName()),
                        valueOf(item.scoredItemCount()),
                        valueOf(item.comparableScoreCount()),
                        valueOf(item.calibrationScoreCount()),
                        valueOf(item.averageScore()),
                        valueOf(item.minScore()),
                        valueOf(item.maxScore()),
                        valueOf(item.biasFromPeerMean()),
                        valueOf(item.averageAbsoluteDeviation()),
                        valueOf(item.rootMeanSquareDeviation())
                })
                .toList();
        return new ExportContent(
                new String[]{"JudgeUserID", "JudgeName", "ScoredItemCount", "ComparableScoreCount", "CalibrationScoreCount", "AverageScore", "MinScore", "MaxScore", "Bias", "MeanAbsoluteDeviation", "RMSD"},
                rows
        );
    }

    private byte[] writeCsv(String[] header, List<String[]> rows) {
        StringWriter writer = new StringWriter();
        writer.write('\uFEFF');
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeAll(rows);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate research CSV", e);
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String normalizeType(String type) {
        return type == null || type.isBlank() ? "dashboard" : type.trim().toLowerCase();
    }

    private String valueOf(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return String.valueOf(value);
    }

    private record ExportContent(String[] header, List<String[]> rows) {
    }
}
