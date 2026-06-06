package com.fpt.swp.sealhackathonbe.judging.controller;

import com.fpt.swp.sealhackathonbe.judge_assignment.dto.AssignmentDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
public class JudgingController {

    private final JudgingService judgingService;

    @Autowired
    public JudgingController(JudgingService judgingService) {
        this.judgingService = judgingService;
    }

    @PostMapping("/scores")
    public ResponseEntity<Void> recordJudging(@Valid @RequestBody ScoreSubmissionDTO scoreSubmissionDTO) {
        judgingService.recordScore(scoreSubmissionDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/judge/assignments")
    public ResponseEntity<List<AssignmentDTO>> getJudgeAssignments(
            @RequestParam(required = false) UUID judgeId) { // Can be removed if using SecurityContext
        
        // Use a mock ID or fetch from security context later
        UUID id = judgeId != null ? judgeId : UUID.randomUUID(); 
        
        // In a real implementation, you would call:
        // List<AssignmentDTO> assignments = judgingService.getJudgeAssignments(id);
        
        // MOCKED list
        List<AssignmentDTO> assignments = new ArrayList<>();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/judging/submission/{submissionId}")
    public ResponseEntity<List<JudgingDTO>> getJudgingBySubmission(@PathVariable UUID submissionId) {
        List<JudgingDTO> scores = judgingService.getScoresBySubmission(submissionId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/judging/judge/{judgeUserId}")
    public ResponseEntity<List<JudgingDTO>> getJudgngByJudgeId(@PathVariable UUID judgeUserId) {
        List<JudgingDTO> scores = judgingService.getScoresByJudge(judgeUserId);
        return ResponseEntity.ok(scores);
    }
}
