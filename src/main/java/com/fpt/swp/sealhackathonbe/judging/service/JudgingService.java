package com.fpt.swp.sealhackathonbe.judging.service;

import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.Judging;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JudgingService {
    List<JudgingDTO> getScoresBySubmissionAndJudgeId(UUID submissionId);
    List<JudgingDTO> getPublishedScoresBySubmission(UUID submissionId);
    List<JudgingDTO> getBatchScoresBySubmissionIds(com.fpt.swp.sealhackathonbe.judging.dto.BatchScoreRequestDTO request);
    List<JudgingDTO> getScoresByJudgeId(UUID judgeUserId);
    void recordJudging(List<ScoreSubmissionDTO> dtos);
    void updateJudging(List<UpdateScoreSubmissionDTO> dtos);
    Map<UUID, List<Judging>> getJudgingsGroupedBySubmissionIds(List<UUID> submissionIds);
    List<EvaluationAuditLogDTO> getEvaluationAuditLogsByEvent(UUID eventId);
    void rejectSubmissionScores(UUID submissionId, String reason);
    void deleteJudging(UUID submissionId, String reason);
    SubmissionResponse approveScore(UUID submissionId, boolean approve);
    void rejectSubmissionScoreForJudge(UUID submissionId, UUID judgeId, String reason);
    void rejectJudgeScoresInRound(UUID roundId, UUID judgeId, String reason);
}