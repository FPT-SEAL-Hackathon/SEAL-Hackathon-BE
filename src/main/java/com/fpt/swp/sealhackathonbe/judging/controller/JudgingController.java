package com.fpt.swp.sealhackathonbe.judging.controller;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1")
@Validated
public class JudgingController {

    private final JudgingService judgingService;
    private final com.fpt.swp.sealhackathonbe.user.repository.UserRepository userRepository;

    @Autowired
    public JudgingController(JudgingService judgingService, com.fpt.swp.sealhackathonbe.user.repository.UserRepository userRepository) {
        this.judgingService = judgingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/judging")
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    public ResponseEntity<Void> recordJudging(
            @Valid @RequestBody List<ScoreSubmissionDTO> scoreSubmissionDTOs) {
        judgingService.recordJudging(scoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/judging")
    @PreAuthorize("hasAnyAuthority('ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    public ResponseEntity<Void> updateJudging(
            @Valid @RequestBody List<com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO> updateScoreSubmissionDTOs,
            org.springframework.security.core.Authentication authentication) {
        judgingService.updateJudging(updateScoreSubmissionDTOs);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/judging/submission/{submissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    public ResponseEntity<List<JudgingDTO>> getJudgingBySubmission(@PathVariable UUID submissionId) {
        List<JudgingDTO> scores = judgingService.getScoresBySubmission(submissionId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/judge/{judgeUserId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE')")
    public ResponseEntity<List<JudgingDTO>> getJudgingByJudgeId(@PathVariable UUID judgeUserId) {
        List<JudgingDTO> scores = judgingService.getScoresByJudgeId(judgeUserId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/audit-logs/event/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<EvaluationAuditLogDTO>> getEvaluationAuditLogsByEvent(@PathVariable UUID eventId) {
        List<EvaluationAuditLogDTO> logs = judgingService.getEvaluationAuditLogsByEvent(eventId);
        return ResponseEntity.ok(logs);
    }
}