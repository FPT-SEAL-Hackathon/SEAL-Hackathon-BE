package com.fpt.swp.sealhackathonbe.research.controller;

import com.fpt.swp.sealhackathonbe.research.dto.CalibrationSampleResponse;
import com.fpt.swp.sealhackathonbe.research.dto.CreateCalibrationSampleRequest;
import com.fpt.swp.sealhackathonbe.research.dto.DataExportLogResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ResearchDashboardResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ScoreDistributionResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDashboardService;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDataService;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/research")
@RequiredArgsConstructor
@Tag(name = "Research Dashboard Controller", description = "APIs for research analytics and score quality metrics")
public class ResearchDashboardController {
    private final ResearchDashboardService researchDashboardService;
    private final ResearchDataService researchDataService;

    @GetMapping("/events/{eventId}/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get research dashboard", description = "Returns variance report, score distribution, and judge reliability metrics")
    public ResponseEntity<ResearchDashboardResponse> getDashboard(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal bucketSize
    ) {
        return ResponseEntity.ok(researchDashboardService.getDashboard(eventId, roundId, categoryId, bucketSize));
    }

    @GetMapping("/events/{eventId}/variance-report")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get variance report", description = "Shows score variance per submission and criterion")
    public ResponseEntity<List<VarianceReportResponse>> getVarianceReport(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId
    ) {
        return ResponseEntity.ok(researchDashboardService.getVarianceReport(eventId, roundId, categoryId));
    }

    @GetMapping("/events/{eventId}/score-distribution")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get score distribution", description = "Groups non-calibration scores into configurable buckets")
    public ResponseEntity<List<ScoreDistributionResponse>> getScoreDistribution(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal bucketSize
    ) {
        return ResponseEntity.ok(researchDashboardService.getScoreDistribution(eventId, roundId, categoryId, bucketSize));
    }

    @GetMapping("/events/{eventId}/reliability-metrics")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get reliability metrics", description = "Compares each judge's scores against peer means for the same submission and criterion")
    public ResponseEntity<List<ReliabilityMetricResponse>> getReliabilityMetrics(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId
    ) {
        return ResponseEntity.ok(researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId));
    }

    @PostMapping("/calibration-samples")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Create calibration sample", description = "Marks a round submission as a calibration sample with optional reference scores")
    public ResponseEntity<CalibrationSampleResponse> createCalibrationSample(
            @Valid @RequestBody CreateCalibrationSampleRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(researchDataService.createCalibrationSample(request, principal.getUser().getUserId()));
    }

    @GetMapping("/rounds/{roundId}/calibration-samples")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get calibration samples by round", description = "Lists calibration samples configured for a round")
    public ResponseEntity<List<CalibrationSampleResponse>> getCalibrationSamplesByRound(@PathVariable UUID roundId) {
        return ResponseEntity.ok(researchDataService.getCalibrationSamplesByRound(roundId));
    }

    @GetMapping("/calibration-samples/{sampleId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get calibration sample", description = "Retrieves a calibration sample by ID")
    public ResponseEntity<CalibrationSampleResponse> getCalibrationSample(@PathVariable UUID sampleId) {
        return ResponseEntity.ok(researchDataService.getCalibrationSample(sampleId));
    }

    @DeleteMapping("/calibration-samples/{sampleId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Delete calibration sample", description = "Deletes a calibration sample")
    public ResponseEntity<Void> deleteCalibrationSample(@PathVariable UUID sampleId) {
        researchDataService.deleteCalibrationSample(sampleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/export-logs")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get research export logs", description = "Lists CSV exports generated for a research event")
    public ResponseEntity<List<DataExportLogResponse>> getExportLogs(@PathVariable UUID eventId) {
        return ResponseEntity.ok(researchDataService.getExportLogs(eventId));
    }

    @GetMapping(value = "/events/{eventId}/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Export research data CSV", description = "Exports dashboard, variance report, score distribution, or reliability metrics and writes a DataExportLog record")
    public ResponseEntity<byte[]> exportResearchData(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal bucketSize,
            @RequestParam(defaultValue = "dashboard") String type,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DownloadFileResponse file = researchDataService.exportResearchData(
                eventId,
                roundId,
                categoryId,
                bucketSize,
                type,
                principal.getUser().getUserId()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getContent().length)
                .body(file.getContent());
    }
}
