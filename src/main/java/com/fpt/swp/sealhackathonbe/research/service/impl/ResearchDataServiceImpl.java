package com.fpt.swp.sealhackathonbe.research.service.impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.research.dto.CalibrationSampleResponse;
import com.fpt.swp.sealhackathonbe.research.dto.CreateCalibrationSampleRequest;
import com.fpt.swp.sealhackathonbe.research.dto.DataExportLogResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ScoreDistributionResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;
import com.fpt.swp.sealhackathonbe.research.entity.CalibrationSample;
import com.fpt.swp.sealhackathonbe.research.entity.DataExportLog;
import com.fpt.swp.sealhackathonbe.research.repository.CalibrationSampleRepository;
import com.fpt.swp.sealhackathonbe.research.repository.DataExportLogRepository;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDashboardService;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDataService;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
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

    private final CalibrationSampleRepository calibrationSampleRepository;
    private final DataExportLogRepository dataExportLogRepository;
    private final RoundRepository roundRepository;
    private final SubmissionsRepository submissionsRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ResearchDashboardService researchDashboardService;

    @Override
    @Transactional
    public CalibrationSampleResponse createCalibrationSample(CreateCalibrationSampleRequest request, UUID currentUserId) {
        Round round = roundRepository.findById(request.roundId())
                .orElseThrow(() -> new RuntimeException("Round not found"));
        Submissions submission = submissionsRepository.findById(request.submissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        User addedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.roundId().equals(submission.getRoundId())) {
            throw new IllegalArgumentException("Submission does not belong to this round");
        }
        if (calibrationSampleRepository.existsByRound_RoundIdAndSubmission_SubmissionId(
                request.roundId(),
                request.submissionId()
        )) {
            throw new IllegalArgumentException("Calibration sample already exists for this round and submission");
        }

        CalibrationSample sample = new CalibrationSample();
        sample.setRound(round);
        sample.setSubmission(submission);
        sample.setReferenceScoreJson(request.referenceScoreJson());
        sample.setAddedBy(addedBy);

        return toCalibrationResponse(calibrationSampleRepository.save(sample));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalibrationSampleResponse> getCalibrationSamplesByRound(UUID roundId) {
        return calibrationSampleRepository.findByRound_RoundIdOrderByAddedAtDesc(roundId).stream()
                .map(this::toCalibrationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalibrationSampleResponse getCalibrationSample(UUID sampleId) {
        return calibrationSampleRepository.findById(sampleId)
                .map(this::toCalibrationResponse)
                .orElseThrow(() -> new RuntimeException("Calibration sample not found"));
    }

    @Override
    @Transactional
    public void deleteCalibrationSample(UUID sampleId) {
        if (!calibrationSampleRepository.existsById(sampleId)) {
            throw new RuntimeException("Calibration sample not found");
        }
        calibrationSampleRepository.deleteById(sampleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataExportLogResponse> getExportLogs(UUID eventId) {
        return dataExportLogRepository.findByEvent_EventIdOrderByExportedAtDesc(eventId).stream()
                .map(this::toExportLogResponse)
                .toList();
    }

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
                        valueOf(item.biasFromPeerMean()),
                        valueOf(item.averageAbsoluteDeviation()),
                        valueOf(item.rootMeanSquareDeviation())
                })
                .toList();
        return new ExportContent(
                new String[]{"JudgeUserID", "JudgeName", "ScoredItemCount", "ComparableScoreCount", "CalibrationScoreCount", "AverageScore", "BiasFromPeerMean", "AverageAbsoluteDeviation", "RootMeanSquareDeviation"},
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

    private CalibrationSampleResponse toCalibrationResponse(CalibrationSample sample) {
        Submissions submission = sample.getSubmission();
        return new CalibrationSampleResponse(
                sample.getSampleId(),
                sample.getRound().getRoundId(),
                sample.getRound().getRoundName(),
                submission.getSubmissionId(),
                submission.getTeamId(),
                submission.getTeam() == null ? null : submission.getTeam().getTeamName(),
                sample.getReferenceScoreJson(),
                sample.getAddedBy().getUserId(),
                sample.getAddedBy().getFullName(),
                sample.getAddedAt()
        );
    }

    private DataExportLogResponse toExportLogResponse(DataExportLog log) {
        return new DataExportLogResponse(
                log.getExportId(),
                log.getEvent().getEventId(),
                log.getEvent().getEventName(),
                log.getExportedBy().getUserId(),
                log.getExportedBy().getFullName(),
                log.getExportedAt(),
                log.getFileFormat(),
                log.getRowCount(),
                log.getNotes()
        );
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
