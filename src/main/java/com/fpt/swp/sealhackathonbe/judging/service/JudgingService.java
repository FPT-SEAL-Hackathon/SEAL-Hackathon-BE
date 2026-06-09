package com.fpt.swp.sealhackathonbe.judging.service;

import com.fpt.swp.sealhackathonbe.judge_assignment.dto.AssignmentDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;

import java.util.List;
import java.util.UUID;

public interface JudgingService {
<<<<<<< Updated upstream
    void recordScore(ScoreSubmissionDTO dto);
    List<JudgingDTO> getScoresBySubmission(UUID submissionId);
    List<JudgingDTO> getScoresByJudge(UUID judgeUserId);
    List<AssignmentDTO> getJudgeAssignments(UUID judgeUserId);
=======
    void recordJudging(ScoreSubmissionDTO dto);
    List<JudgingDTO> getScoresBySubmission(UUID submissionId);
    List<JudgingDTO> getScoresByJudge(UUID judgeUserId);
>>>>>>> Stashed changes
}
