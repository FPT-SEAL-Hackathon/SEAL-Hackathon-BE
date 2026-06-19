package com.fpt.swp.sealhackathonbe.judging.controller;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "Judging Controller", description = "APIs for record judge and query judge")
public class JudgingController {
    private final JudgingService judgingService;

    @PostMapping("/judging")
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    @Operation(summary = "Record judging scores", description = "Allows internal and guest judges to submit scores for a submission")
    public ResponseEntity<Void> recordJudging(
            @Valid @RequestBody List<ScoreSubmissionDTO> scoreSubmissionDTOs) {
        judgingService.recordJudging(scoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/judging")
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    @Operation(summary = "Update judging scores", description = "Allows internal and guest judges to update previously submitted scores")
    public ResponseEntity<Void> updateJudging(
            @Valid @RequestBody List<UpdateScoreSubmissionDTO> updateScoreSubmissionDTOs,
            org.springframework.security.core.Authentication authentication) {
        judgingService.updateJudging(updateScoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/judging/submission/{submissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    @Operation(summary = "Get judging scores by submission ID", description = "Retrieves all scores given to a specific submission")
    public ResponseEntity<List<JudgingDTO>> getJudgingBySubmission(@PathVariable UUID submissionId) {
        List<JudgingDTO> scores = judgingService.getScoresBySubmission(submissionId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/judge/{judgeUserId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    @Operation(summary = "Get judging scores by judge ID", description = "Retrieves all scores given by a specific judge")
    public ResponseEntity<List<JudgingDTO>> getJudgingByJudgeId(@PathVariable UUID judgeUserId) {
        List<JudgingDTO> scores = judgingService.getScoresByJudgeId(judgeUserId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/audit-logs/event/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Get evaluation audit logs by event ID", description = "Retrieves audit logs for score changes in an event")
    public ResponseEntity<List<EvaluationAuditLogDTO>> getEvaluationAuditLogsByEvent(@PathVariable UUID eventId) {
        List<EvaluationAuditLogDTO> logs = judgingService.getEvaluationAuditLogsByEvent(eventId);
        return ResponseEntity.ok(logs);
    }
}