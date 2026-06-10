package com.fpt.swp.sealhackathonbe.judging.service;

import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;

import java.util.List;
import java.util.UUID;

public interface JudgingService {
    List<JudgingDTO> getScoresBySubmission(UUID submissionId);
    List<JudgingDTO> getScoresByJudgeId(UUID judgeUserId);
    void recordJudging(ScoreSubmissionDTO dto);
}
