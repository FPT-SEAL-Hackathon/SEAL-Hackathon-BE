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
import java.util.List;
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

    private ExportContent buildDashboardExport(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize) {
        List<String[]> rows = new ArrayList<>();
        for (VarianceReportResponse item : researchDashboardService.getVarianceReport(eventId, roundId, categoryId)) {
            rows.add(new String[]{
                    "variance-report",
                    valueOf(item.roundId()),
                    valueOf(item.categoryId()),
                    valueOf(item.submissionId()),
                    valueOf(item.teamId()),
                    valueOf(item.roundCriterionId()),
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
                    valueOf(item.judgeUserId()),
                    valueOf(item.judgeName()),
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
