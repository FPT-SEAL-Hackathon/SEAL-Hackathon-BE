package com.fpt.swp.sealhackathonbe.judging.controller;

import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class JudgingController {

    private final JudgingService judgingService;

    @Autowired
    public JudgingController(JudgingService judgingService) {
        this.judgingService = judgingService;
    }

    @PostMapping("/judging")
    public ResponseEntity<Void> recordJudging(@Valid @RequestBody ScoreSubmissionDTO scoreSubmissionDTO) {
        judgingService.recordJudging(scoreSubmissionDTO);
        return ResponseEntity.ok().build();
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
