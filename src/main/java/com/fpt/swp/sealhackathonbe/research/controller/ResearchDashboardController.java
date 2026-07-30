package com.fpt.swp.sealhackathonbe.research.controller;

import com.fpt.swp.sealhackathonbe.research.dto.ConsensusMatrixResponse;
import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.dto.VarianceReportResponse;
import com.fpt.swp.sealhackathonbe.research.service.ResearchDataService;
import com.fpt.swp.sealhackathonbe.research.service.impl.ResearchDashboardServiceImpl;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/research")
@RequiredArgsConstructor
@Tag(name = "Research Dashboard Controller", description = "APIs for research analytics and score quality metrics")
public class ResearchDashboardController {
    private final ResearchDataService researchDataService;
    private final ResearchDashboardServiceImpl researchDashboardService;

    @GetMapping({"/calibration-metrics", "/reliability-metrics"})
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Get calibration metrics", description = "Returns reliability/calibration metrics for judges as JSON")
    public ResponseEntity<java.util.List<ReliabilityMetricResponse>> getCalibrationMetrics(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID roundId
    ) {
        return ResponseEntity.ok(researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId));
    }

    /**
     * Phuong sai diem giua cac giam khao theo TUNG TIEU CHI cua tung bai nop.
     * Du lieu nay da duoc tinh san trong getVarianceReport tu truoc nhung chua he co endpoint
     * JSON nao expose ra, nen dashboard khong hien thi duoc — chi lay gian tiep qua CSV.
     * Chi ORGANIZER/ADMIN: no lo ra bai nop nao bi cham lech nhieu (kem ten doi), khong phai
     * thong tin danh cho giam khao.
     */
    @GetMapping("/variance-report")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Get score variance by criterion",
            description = "Returns per-submission, per-criterion score variance across judges (excludes calibration scores)")
    public ResponseEntity<List<VarianceReportResponse>> getVarianceReport(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId
    ) {
        return ResponseEntity.ok(researchDashboardService.getVarianceReport(eventId, roundId, categoryId));
    }

    @GetMapping(value = "/events/{eventId}/export", produces = "text/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
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

    @GetMapping({"/calibration/matrix/{roundId}"})
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Get consensus matrix",
            description = "Returns consensus matrix per sample submission and criterion. Judges only see samples they have already scored")
    public ResponseEntity<List<ConsensusMatrixResponse>> getConsensusMatrix(
            @PathVariable UUID roundId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Organizer/Admin xem toan bo; giam khao chi xem bai mau ma ho DA cham, de khong the
        // nhin median roi cham theo (anchoring) — xem ghi chu o getConsensusMatrix.
        boolean revealAll = hasAnyRole(principal, "ROLE_ORGANIZER", "ROLE_ADMIN");
        UUID viewerId = principal != null && principal.getUser() != null
                ? principal.getUser().getUserId()
                : null;
        return ResponseEntity.ok(researchDashboardService.getConsensusMatrix(roundId, viewerId, revealAll));
    }

    private boolean hasAnyRole(UserPrincipal principal, String... roles) {
        if (principal == null || principal.getAuthorities() == null) {
            return false;
        }
        Set<String> granted = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (String role : roles) {
            if (granted.contains(role)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping(value = "/calibration/export/{roundId}", produces = "text/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Export calibration CSV", description = "Exports wide-format CSV for calibration grading")
    public ResponseEntity<byte[]> exportCalibrationCsv(
            @PathVariable UUID roundId
    ) {
        String csv = researchDashboardService.exportCalibrationCsv(roundId);
        byte[] content = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calibration_round_" + roundId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(content.length)
                .body(content);
    }
}
