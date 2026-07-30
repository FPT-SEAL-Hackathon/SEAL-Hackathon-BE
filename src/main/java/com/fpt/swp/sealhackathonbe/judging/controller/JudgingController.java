package com.fpt.swp.sealhackathonbe.judging.controller;

import java.util.List;
import java.util.UUID;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.swp.sealhackathonbe.judging.dto.BatchScoreRequestDTO;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
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
    public ResponseEntity<Map<String, String>> recordJudging(
            @Valid @RequestBody List<ScoreSubmissionDTO> scoreSubmissionDTOs) {
        judgingService.recordJudging(scoreSubmissionDTOs);
        return ResponseEntity.ok(Map.of("message", "Scores have been recorded successfully by the Server."));
    }

    @PatchMapping("/judging")
    // RBAC:
    // Chỉ judge nội bộ/khách được cập nhật điểm đã chấm.
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Update judging scores", description = "Allows internal and guest judges to update previously submitted scores")
    public ResponseEntity<Map<String, String>> updateJudging(
            @Valid @RequestBody List<UpdateScoreSubmissionDTO> updateScoreSubmissionDTOs,
            Authentication authentication) {
        judgingService.updateJudging(updateScoreSubmissionDTOs);
        return ResponseEntity.ok(Map.of("message", "Scores have been updated successfully by the Server."));
    }

    @DeleteMapping("/judging/submission/{submissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    @Operation(summary = "Delete judging scores", description = "Allows judges to delete their previously submitted scores for a submission")
    public ResponseEntity<Map<String, String>> deleteJudging(
            @PathVariable("submissionId") UUID submissionId,
            @RequestParam(value = "reason", required = false) String reason) {
        judgingService.deleteJudging(submissionId, reason);
        return ResponseEntity.ok(Map.of("message", "Scores have been deleted successfully."));
    }

    @PostMapping("/judging/batch-scores")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Get batch judging scores", description = "Retrieves scores for multiple submissions")
    public ResponseEntity<List<JudgingDTO>> getBatchScoresBySubmissions(
            @Valid @RequestBody BatchScoreRequestDTO request) {
        List<JudgingDTO> scores = judgingService.getBatchScoresBySubmissionIds(request);
        return ResponseEntity.ok(scores);
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

    @GetMapping("/judging/team-submission/{submissionId}/published")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Get published judging scores for a submission", description = "Retrieves published scores for a team's submission")
    public ResponseEntity<List<JudgingDTO>> getPublishedScoresBySubmission(@PathVariable UUID submissionId) {
        List<JudgingDTO> scores = judgingService.getPublishedScoresBySubmission(submissionId);
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
    // ORGANIZER (vận hành) và ADMIN (giám sát hệ thống) được xem audit log điểm của event.
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
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

    @Operation(
            summary = "Approve score",
            description = "Approve or unapprove a submission's judging score"
    )
    @PostMapping("/admin/submissions/{submissionId}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<SubmissionResponse> approveScore(
            @PathVariable UUID submissionId,
            @RequestBody java.util.Map<String, Boolean> request
    ) {
        boolean approve = request.getOrDefault("approve", true);
        SubmissionResponse response = judgingService.approveScore(submissionId, approve);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Reject score",
            description = "Reject a submission's judging score and require judges to score again"
    )
    @PostMapping("/admin/submissions/{submissionId}/reject-score")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> rejectScore(
            @PathVariable UUID submissionId,
            @RequestBody java.util.Map<String, String> request
    ) {
        String reason = request.getOrDefault("reason", "Scores rejected by admin");
        judgingService.rejectSubmissionScores(submissionId, reason);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Reject score for a specific judge and submission",
            description = "Reject a judge's score for a specific submission"
    )
    @PostMapping("/admin/submissions/{submissionId}/judges/{judgeId}/reject-score")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> rejectSubmissionScoreForJudge(
            @PathVariable UUID submissionId,
            @PathVariable UUID judgeId,
            @RequestBody java.util.Map<String, String> request
    ) {
        String reason = request.getOrDefault("reason", "Judge scores rejected by admin");
        judgingService.rejectSubmissionScoreForJudge(submissionId, judgeId, reason);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Reject all scores of a judge in a round",
            description = "Reject all scores submitted by a judge in a round"
    )
    @PostMapping("/admin/rounds/{roundId}/judges/{judgeId}/reject-scores")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> rejectJudgeScoresInRound(
            @PathVariable UUID roundId,
            @PathVariable UUID judgeId,
            @RequestBody java.util.Map<String, String> request
    ) {
        String reason = request.getOrDefault("reason", "All judge scores in round rejected by admin");
        judgingService.rejectJudgeScoresInRound(roundId, judgeId, reason);
        return ResponseEntity.ok().build();
    }
}
