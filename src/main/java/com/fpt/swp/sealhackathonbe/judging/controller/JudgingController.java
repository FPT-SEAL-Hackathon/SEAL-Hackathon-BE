package com.fpt.swp.sealhackathonbe.judging.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.research.dto.ReliabilityMetricResponse;
import com.fpt.swp.sealhackathonbe.research.service.impl.ResearchDashboardServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "Judging Controller", description = "APIs for record judge and query judge")
public class JudgingController {
    private final JudgingService judgingService;
    private final ResearchDashboardServiceImpl researchDashboardService;

    @PostMapping("/judging")
    // RBAC:
    // Chỉ judge nội bộ/khách được ghi điểm.
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Record judging scores", description = "Allows internal and guest judges to submit scores for a submission")
    public ResponseEntity<Void> recordJudging(
            @Valid @RequestBody List<ScoreSubmissionDTO> scoreSubmissionDTOs) {
        judgingService.recordJudging(scoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/judging")
    // RBAC:
    // Chỉ judge nội bộ/khách được cập nhật điểm đã chấm.
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Update judging scores", description = "Allows internal and guest judges to update previously submitted scores")
    public ResponseEntity<Void> updateJudging(
            @Valid @RequestBody List<UpdateScoreSubmissionDTO> updateScoreSubmissionDTOs,
            org.springframework.security.core.Authentication authentication) {
        judgingService.updateJudging(updateScoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/judging/submission/{submissionId}")
    // RBAC:
    // ORGANIZER và judge được xem điểm của submission phục vụ quản lý/chấm.
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Get judging scores by submission ID", description = "Retrieves all scores given to a specific submission")
    public ResponseEntity<List<JudgingDTO>> getJudgingBySubmission(@PathVariable UUID submissionId) {
        List<JudgingDTO> scores = judgingService.getScoresBySubmissionAndJudgeId(submissionId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/judge/{judgeUserId}")
    // RBAC:
    // Cho phép ORGANIZER hoặc chính judge xem lịch sử chấm của mình.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER') or #judgeUserId == principal.user.userId")
    @Operation(summary = "Get judging scores by judge ID", description = "Retrieves all scores given by a specific judge")
    public ResponseEntity<List<JudgingDTO>> getJudgingByJudgeId(@PathVariable UUID judgeUserId) {
        List<JudgingDTO> scores = judgingService.getScoresByJudgeId(judgeUserId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/audit-logs/event/{eventId}")
    // RBAC:
    // Chỉ ORGANIZER được xem audit log điểm của event.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get evaluation audit logs by event ID", description = "Retrieves audit logs for score changes in an event")
    public ResponseEntity<List<EvaluationAuditLogDTO>> getEvaluationAuditLogsByEvent(@PathVariable UUID eventId) {
        List<EvaluationAuditLogDTO> logs = judgingService.getEvaluationAuditLogsByEvent(eventId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping(value = "/judging/events/{eventId}/calibration-metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Get calibration metrics", description = "Returns calibration metrics for all judges in the event as JSON")
    public ResponseEntity<List<ReliabilityMetricResponse>> getCalibrationMetrics(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId
    ) {
        return ResponseEntity.ok(researchDashboardService.getReliabilityMetrics(eventId, roundId, categoryId));
    }
}
