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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        ExportContent exportContent = buildExportContent(eventId, roundId, categoryId, bucketSize, type);
        byte[] content = writeCsv(exportContent.header(), exportContent.rows());

        DataExportLog log = new DataExportLog();
        log.setEvent(event);
        log.setExportedBy(exportedBy);
        log.setFileFormat("CSV");
        log.setRowCount(exportContent.rows().size());
        log.setNotes("Research export: " + normalizeType(type));
        dataExportLogRepository.save(log);

        String filename = "research-" + normalizeType(type) + "-" + eventId + ".csv";
        return new DownloadFileResponse(filename, CSV_CONTENT_TYPE, content);
    }

    private ExportContent buildExportContent(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize, String type) {
        return switch (normalizeType(type)) {
            case "dashboard" -> buildDashboardExport(eventId, roundId, categoryId, bucketSize);
            case "variance-report" -> buildVarianceExport(eventId, roundId, categoryId);
            case "score-distribution" -> buildDistributionExport(eventId, roundId, categoryId, bucketSize);
            case "reliability-metrics" -> buildReliabilityExport(eventId, roundId, categoryId);
            default -> throw new IllegalArgumentException("Unsupported research export type: " + type);
        };
    }

    private String getPseudoId(Map<UUID, String> map, UUID id, String prefix) {
        if (id == null) return "";
        return map.computeIfAbsent(id, k -> prefix + "_" + (map.size() + 1));
    }

    private ExportContent buildDashboardExport(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        List<String[]> rows = new ArrayList<>();
        Map<UUID, String> roundMap = new HashMap<>();
        Map<UUID, String> categoryMap = new HashMap<>();
        Map<UUID, String> submissionMap = new HashMap<>();
        Map<UUID, String> teamMap = new HashMap<>();
        Map<UUID, String> criterionMap = new HashMap<>();
        Map<UUID, String> judgeMap = new HashMap<>();

        for (VarianceReportResponse item : researchDashboardService.getVarianceReport(eventId, roundId, categoryId)) {
            rows.add(new String[]{
                    "variance-report",
                    getPseudoId(roundMap, item.roundId(), "ROUND"),
                    getPseudoId(categoryMap, item.categoryId(), "TRACK"),
                    getPseudoId(submissionMap, item.submissionId(), "SUBMISSION"),
                    getPseudoId(teamMap, item.teamId(), "TEAM"),
                    getPseudoId(criterionMap, item.roundCriterionId(), "CRITERION"),
                    valueOf(item.judgeCount()),
                    valueOf(item.meanScore()),
                    valueOf(item.standardDeviation()),
                    valueOf(item.scoreRange()),
                    valueOf(item.variance())
            });
        }
        for (ScoreDistributionResponse item : researchDashboardService.getScoreDistribution(eventId, roundId, categoryId, bucketSize)) {
            rows.add(new String[]{
                    "score-distribution",
                    "",
                    "",
                    "",
                    "",
                    "",
                    valueOf(item.scoreCount()),
                    valueOf(item.bucketStart()),
                    valueOf(item.bucketEnd()),
                    valueOf(item.percentage()),
                    ""
            });
        }
        for (ReliabilityMetricResponse item : researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId)) {
            rows.add(new String[]{
                    "reliability-metrics",
                    getPseudoId(judgeMap, item.judgeUserId(), "JUDGE"),
                    getPseudoId(judgeMap, item.judgeUserId(), "JUDGE"),
                    "",
                    "",
                    "",
                    valueOf(item.scoredItemCount()),
                    valueOf(item.averageScore()),
                    valueOf(item.biasFromPeerMean()),
                    valueOf(item.averageAbsoluteDeviation()),
                    valueOf(item.rootMeanSquareDeviation())
            });
        }

        // Add notes section for context and definitions
        rows.add(new String[]{"", "", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"NOTES & DEFINITIONS", "", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"Data Anonymization", "All identifying UUIDs and names have been replaced with pseudo-IDs (e.g., TEAM_1, JUDGE_1) for research purposes.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"variance-report", "Analyzes the spread of scores for a single submission across different judges.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Count: Number of judges who scored this criterion for this submission.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric1 (MeanScore): Average score given by all judges for this criterion.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric2 (StdDev): Standard deviation indicating how spread out the scores are.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric3 (ScoreRange): Difference between highest and lowest score.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric4 (Variance): The variance of the scores, indicating judge agreement.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"score-distribution", "Shows how many scores fall into specific score buckets.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Count: Number of scores in this bucket.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric1 (BucketStart): Start of the score bucket.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric2 (BucketEnd): End of the score bucket.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric3 (Percentage): Percentage of total scores in this bucket.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"reliability-metrics", "Evaluates individual judge performance compared to their peers.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Count: Number of items scored by this judge.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric1 (AverageScore): The judge's average score across all their gradings.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric2 (BiasFromPeerMean): How much higher/lower this judge scores compared to the average of other judges.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric3 (AAD): Average Absolute Deviation from the peer mean.", "", "", "", "", "", "", "", "", ""});
        rows.add(new String[]{"", "- Metric4 (RMSD): Root Mean Square Deviation from the peer mean (penalizes large outliers more).", "", "", "", "", "", "", "", "", ""});

        return new ExportContent(
                new String[]{"Section", "RefID1", "RefID2", "SubmissionID", "TeamID", "CriterionID", "Count", "Metric1", "Metric2", "Metric3", "Metric4"},
                rows
        );
    }

    private ExportContent buildVarianceExport(UUID eventId, UUID roundId, UUID categoryId) {
        List<String[]> rows = researchDashboardService.getVarianceReport(eventId, roundId, categoryId).stream()
                .map(item -> new String[]{
                        valueOf(item.roundId()),
                        valueOf(item.roundName()),
                        valueOf(item.categoryId()),
                        valueOf(item.categoryName()),
                        valueOf(item.submissionId()),
                        valueOf(item.teamId()),
                        valueOf(item.teamName()),
                        valueOf(item.roundCriterionId()),
                        valueOf(item.criterionName()),
                        valueOf(item.judgeCount()),
                        valueOf(item.meanScore()),
                        valueOf(item.standardDeviation()),
                        valueOf(item.scoreRange()),
                        valueOf(item.variance())
                })
                .toList();
        return new ExportContent(
                new String[]{"RoundID", "RoundName", "CategoryID", "CategoryName", "SubmissionID", "TeamID", "TeamName", "RoundCriterionID", "CriterionName", "JudgeCount", "MeanScore", "StandardDeviation", "ScoreRange", "Variance"},
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
                new String[]{"BucketStart", "BucketEnd", "ScoreCount", "Percentage"},
                rows
        );
    }

    private ExportContent buildReliabilityExport(UUID eventId, UUID roundId, UUID categoryId) {
        List<String[]> rows = researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId).stream()
                .map(item -> new String[]{
                        valueOf(item.judgeUserId()),
                        valueOf(item.judgeName()),
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
                new String[]{"JudgeUserID", "JudgeName", "ScoredItemCount", "ComparableScoreCount", "CalibrationScoreCount", "AverageScore", "MinScore", "MaxScore", "BiasFromPeerMean", "AverageAbsoluteDeviation", "RootMeanSquareDeviation"},
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
