package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.*;
import com.fpt.swp.sealhackathonbe.judging.repository.*;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JudgingServiceImpl implements JudgingService {

    private final JudgingRepository judgingRepository;
    private final SubmissionsRepository submissionRepository;
    private final EvaluationAuditLogRepository evaluationAuditLogRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    private final AuthenticationServiceImpl authenticationServiceImpl;
    private final com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService roundJudgeService;
    private final com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository roundJudgeRepository;


    @Override
    @Transactional
    public void recordJudging(List<ScoreSubmissionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Score submission list cannot be empty");
        }

        // We assume all dtos in the batch are for the same submission
        ScoreSubmissionDTO firstDto = dtos.get(0);

        // 1. Fetch & validate that the submission exists
        Submissions submission = submissionRepository.findById(firstDto.getSubmissionId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with ID: " + firstDto.getSubmissionId()));

        // 2. Fetch & validate that the actor (audit user) exists
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new org.springframework.security.access.AccessDeniedException("Actor not found from token");
        }

        // 3. Verify that the actor is a judge in this round using RoundJudgeService
        List<JudgeResponse> judgesInRound = roundJudgeService.getJudgesByRound(submission.getRoundId());
        boolean isJudge = judgesInRound.stream().anyMatch(j -> j.getJudgeId().equals(actor.getUserId()));
        if (!isJudge) {
            throw new org.springframework.security.access.AccessDeniedException("You are not assigned as a judge for this round.");
        }

        // Fetch the RoundJudge entity
        RoundJudge judge = roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(actor.getUserId(), submission.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("RoundJudge entity not found for this round and user."));

        // 4. Extract Team and Event from the submission hierarchy
        Teams team = submission.getTeam();
        Event event = (team != null) ? team.getEvent() : null;
        if (event == null) {
            throw new IllegalStateException("Could not log evaluation audit because the submission's event context is missing.");
        }

        List<Judging> newJudgings = new ArrayList<>();
        List<EvaluationAuditLog> auditLogs = new ArrayList<>();

        for (ScoreSubmissionDTO dto : dtos) {
            if (!dto.getSubmissionId().equals(submission.getSubmissionId())) {
                throw new IllegalArgumentException("All scores in a batch must belong to the same submission.");
            }

            // 5. Fetch & validate that the event criterion exists
            RoundCriterion criterion = roundCriterionRepository.findById(dto.getRoundCriterionId())
                    .orElseThrow(() -> new EntityNotFoundException("Round Criterion not found with ID: " + dto.getRoundCriterionId()));

            // 6. Validate that the score value does not exceed the maximum allowed value
            if (dto.getScoreValue().compareTo(criterion.getMaxScore()) > 0) {
                throw new IllegalArgumentException(String.format(
                        "Score value %s exceeds the maximum allowed value %s for criterion '%s'.",
                        dto.getScoreValue(), criterion.getMaxScore(), criterion.getCriterionName()
                ));
            }

            // 7. Check if a score already exists for this submission, judge, and criterion
            Optional<Judging> existingScoreOpt = judgingRepository
                    .findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
                            dto.getSubmissionId(), judge.getRoundJudgeId(), dto.getRoundCriterionId()
                    );

            if (existingScoreOpt.isPresent()) {
                throw new IllegalStateException("A score for criterion '" + criterion.getCriterionName() + "' already exists. Please use the update API.");
            }

            Judging newJudging = new Judging();
            newJudging.setSubmission(submission);
            newJudging.setRoundJudge(judge);
            newJudging.setRoundCriterion(criterion);
            newJudging.setScoreValue(dto.getScoreValue());
            newJudging.setComment(dto.getComment());
            newJudging.setIsCalibration(dto.getIsCalibration() != null ? dto.getIsCalibration() : false);
            newJudgings.add(newJudging);

            String formattedNewComment = dto.getComment() != null ? dto.getComment().replace("\"", "\\\"") : "";
            String newValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", dto.getScoreValue(), formattedNewComment);

            EvaluationAuditLog auditLog = new EvaluationAuditLog();
            auditLog.setEvent(event);
            auditLog.setActionType("SCORE_CREATED");
            auditLog.setActor(actor);
            auditLog.setScore(newJudging);
            auditLog.setTeam(team);
            auditLog.setSubmission(submission);
            auditLog.setOldValue(null);
            auditLog.setNewValue(newValue);
            auditLog.setReason("Initial score submission");
            auditLogs.add(auditLog);
        }

        judgingRepository.saveAll(newJudgings);
        evaluationAuditLogRepository.saveAll(auditLogs);
    }

    @Override
    @Transactional
    public void updateJudging(List<com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Score update list cannot be empty");
        }

        // 1. Fetch & validate that the actor exists
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new org.springframework.security.access.AccessDeniedException("Actor not found from token");
        }

        List<Judging> updatedJudgings = new ArrayList<>();
        List<EvaluationAuditLog> auditLogs = new ArrayList<>();

        for (com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO dto : dtos) {
            // 2. Fetch & validate that the judging exists
            Judging existingJudging = judgingRepository.findById(dto.getJudgingId())
                    .orElseThrow(() -> new EntityNotFoundException("Judging not found with ID: " + dto.getJudgingId()));

            // 3. Verify that the actor is the one who created the score
            if (!existingJudging.getRoundJudge().getJudge().getUserId().equals(actor.getUserId())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update judging ID: " + dto.getJudgingId());
            }

            RoundCriterion criterion = existingJudging.getRoundCriterion();

            // 4. Validate that the score value does not exceed the maximum allowed value
            if (dto.getScoreValue() != null && dto.getScoreValue().compareTo(criterion.getMaxScore()) > 0) {
                throw new IllegalArgumentException(String.format(
                        "Score value %s exceeds the maximum allowed value %s for criterion '%s'.",
                        dto.getScoreValue(), criterion.getMaxScore(), criterion.getCriterionName()
                ));
            }

            String actionType = "SCORE_UPDATED";

            String formattedOldComment = existingJudging.getComment() != null ? existingJudging.getComment().replace("\"", "\\\"") : "";
            String oldValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", existingJudging.getScoreValue(), formattedOldComment);

            // 5. Apply partial updates (PATCH)
            if (dto.getScoreValue() != null) {
                existingJudging.setScoreValue(dto.getScoreValue());
            }
            if (dto.getComment() != null) {
                existingJudging.setComment(dto.getComment());
            }
            if (dto.getIsCalibration() != null) {
                existingJudging.setIsCalibration(dto.getIsCalibration());
            }

            String formattedNewComment = existingJudging.getComment() != null ? existingJudging.getComment().replace("\"", "\\\"") : "";
            String newValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", existingJudging.getScoreValue(), formattedNewComment);

            updatedJudgings.add(existingJudging);

            // 6. Extract Team and Event
            Submissions submission = existingJudging.getSubmission();
            Teams team = submission.getTeam();
            Event event = (team != null) ? team.getEvent() : null;

            if (event == null) {
                throw new IllegalStateException("Could not log evaluation audit because the submission's event context is missing.");
            }

            // 7. Create and save the EvaluationAuditLog
            EvaluationAuditLog auditLog = new EvaluationAuditLog();
            auditLog.setEvent(event);
            auditLog.setActionType(actionType);
            auditLog.setActor(actor);
            auditLog.setScore(existingJudging);
            auditLog.setTeam(team);
            auditLog.setSubmission(submission);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLog.setReason(dto.getReason());
            auditLogs.add(auditLog);
        }

        judgingRepository.saveAll(updatedJudgings);
        evaluationAuditLogRepository.saveAll(auditLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getScoresBySubmission(UUID submissionId) {
        return judgingRepository.findBySubmission_SubmissionId(submissionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getScoresByJudgeId(UUID roundJudgeId) {
        return judgingRepository.findByRoundJudge_RoundJudgeId(roundJudgeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<Judging>> getJudgingsGroupedBySubmissionIds(List<UUID> submissionIds) {

        if (submissionIds == null || submissionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Judging> allJudgings = judgingRepository.findBySubmission_SubmissionIdIn(submissionIds);

        return allJudgings.stream()
                .collect(Collectors.groupingBy(j -> j.getSubmission().getSubmissionId()));
    }
    private JudgingDTO convertToDTO(Judging judging) {
        return JudgingDTO.builder()
                .id(judging.getId())
                .submissionId(judging.getSubmission() != null ? judging.getSubmission().getSubmissionId() : null)
                .roundJudgeId(judging.getRoundJudge() != null ? judging.getRoundJudge().getJudge().getUserId() : null)
                .judgeName(judging.getRoundJudge() != null ? judging.getRoundJudge().getJudge().getFullName() : null)
                .roundCriterionId(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getRoundCriterionId() : null)
                .criterionName(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getCriterionName() : null)
                .scoreValue(judging.getScoreValue())
                .comment(judging.getComment())
                .scoredAt(judging.getScoredAt())
                .updatedAt(judging.getUpdatedAt())
                .isCalibration(judging.getIsCalibration())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO> getEvaluationAuditLogsByEvent(UUID eventId) {
        return evaluationAuditLogRepository.findByEvent_EventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(log -> com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO.builder()
                        .id(log.getId())
                        .eventId(log.getEvent() != null ? log.getEvent().getEventId() : null)
                        .actionType(log.getActionType())
                        .actorUserId(log.getActor() != null ? log.getActor().getUserId() : null)
                        .judgingId(log.getScore() != null ? log.getScore().getId() : null)
                        .teamId(log.getTeam() != null ? log.getTeam().getTeamId() : null)
                        .submissionId(log.getSubmission() != null ? log.getSubmission().getSubmissionId() : null)
                        .oldValue(log.getOldValue())
                        .newValue(log.getNewValue())
                        .reason(log.getReason())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}