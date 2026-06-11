package com.fpt.swp.sealhackathonbe.judging.service;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.Judging;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JudgingService {
    List<JudgingDTO> getScoresBySubmission(UUID submissionId);
    List<JudgingDTO> getScoresByJudgeId(UUID judgeUserId);
    void recordJudging(ScoreSubmissionDTO dto);
    Map<UUID, List<Judging>> getJudgingsGroupedBySubmissionIds(List<UUID> submissionIds);
    List<EvaluationAuditLogDTO> getEvaluationAuditLogsByEvent(UUID eventId);
}