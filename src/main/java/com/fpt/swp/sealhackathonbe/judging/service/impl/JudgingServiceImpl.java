package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.*;
import com.fpt.swp.sealhackathonbe.judging.repository.*;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.EvaluationAuditLogDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;

@Service
@RequiredArgsConstructor
public class JudgingServiceImpl implements JudgingService {

    private final JudgingRepository judgingRepository;
    private final SubmissionsRepository submissionRepository;
    private final EvaluationAuditLogRepository evaluationAuditLogRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    private final AuthenticationServiceImpl authenticationServiceImpl;
    private final RoundJudgeService roundJudgeService;
    private final RoundJudgeRepository roundJudgeRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final com.fpt.swp.sealhackathonbe.round.repository.RoundRepository roundRepository;

    // Event context của một submission suy từ ROUND (round -> category -> event) để hoạt động
    // cả với sample submission (calibration) vốn có TeamID = null (không lấy được qua team).
    private Event resolveEvent(Submissions submission) {
        return roundRepository.findById(submission.getRoundId())
                .map(r -> r.getCategory() != null ? r.getCategory().getEvent() : null)
                .orElse(null);
    }


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
            throw new AccessDeniedException("Actor not found from token");
        }

        // 3. Verify that the actor is a judge in this round using RoundJudgeService
        List<RoundJudgeResponse> judgesInRound = roundJudgeService.getJudgesByRound(submission.getRoundId());
        boolean isJudge = judgesInRound.stream().anyMatch(j -> j.getJudgeId().equals(actor.getUserId()));
        if (!isJudge) {
            throw new AccessDeniedException("You are not assigned as a judge for this round.");
        }

        // Fetch the RoundJudge entity
        RoundJudge judge = roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(actor.getUserId(), submission.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("RoundJudge entity not found for this round and user."));

        // 4. Check Judging Deadline and 3-layer tight validation
        Round round = judge.getRound();
        if (round != null) {
            LocalDateTime now = LocalDateTime.now();
            
            // Layer 1: Time bounds
            if (round.getStartDate() != null && now.isBefore(round.getStartDate())) {
                throw new IllegalStateException("The judging period for this round has not started yet.");
            }
            if (round.getJudgingDeadline() != null && now.isAfter(round.getJudgingDeadline())) {
                throw new IllegalStateException("The judging deadline for this round has passed.");
            }
            
            // Layer 2: Round Status
            if (round.getRoundStatus() != null && !"Judging".equalsIgnoreCase(round.getRoundStatus().getStatusName())) {
                throw new IllegalStateException("This round is not currently in the 'Judging' phase.");
            }
        }
        
        // Layer 3: Submission Score Finalize check
        if (Boolean.TRUE.equals(submission.getIsScoreApproved())) {
            throw new IllegalStateException("The score for this submission has been finalized by the Organizer and cannot be modified.");
        }

        // 5. Extract Team and Event from the submission hierarchy
        Teams team = submission.getTeam();
        Event event = resolveEvent(submission);
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

            Judging newJudging;
            if (existingScoreOpt.isPresent()) {
                if (Boolean.TRUE.equals(existingScoreOpt.get().getIsActive())) {
                    throw new IllegalStateException("A score for criterion '" + criterion.getCriterionName() + "' already exists. Please use the update API.");
                } else {
                    newJudging = existingScoreOpt.get();
                    newJudging.setIsActive(true);
                }
            } else {
                newJudging = new Judging();
                newJudging.setSubmission(submission);
                newJudging.setRoundJudge(judge);
                newJudging.setRoundCriterion(criterion);
            }
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
        
        // Update submission status to In Progress if it's not Disqualified
        if (!submission.getSubmissionStatusId().equals(SubmissionStatusConstants.DISQUALIFIED)) {
            submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
            submissionRepository.save(submission);
        }
    }

    @Override
    @Transactional
    public void updateJudging(List<UpdateScoreSubmissionDTO> dtos) {
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

        for (UpdateScoreSubmissionDTO dto : dtos) {
            // 2. Fetch & validate that the judging exists
            Judging existingJudging = judgingRepository.findById(dto.getJudgingId())
                    .orElseThrow(() -> new EntityNotFoundException("Judging not found with ID: " + dto.getJudgingId()));

            // 3. Verify that the actor is the one who created the score
            if (!existingJudging.getRoundJudge().getJudge().getUserId().equals(actor.getUserId())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update judging ID: " + dto.getJudgingId());
            }

            RoundCriterion criterion = existingJudging.getRoundCriterion();

            // 4. Check Judging Deadline and 3-layer tight validation
            Round round = existingJudging.getRoundJudge().getRound();
            if (round != null) {
                LocalDateTime now = LocalDateTime.now();
                
                // Layer 1: Time bounds
                if (round.getStartDate() != null && now.isBefore(round.getStartDate())) {
                    throw new IllegalStateException("The judging period for this round has not started yet.");
                }
                if (round.getJudgingDeadline() != null && now.isAfter(round.getJudgingDeadline())) {
                    throw new IllegalStateException("The judging deadline for this round has passed.");
                }
                
                // Layer 2: Round Status
                if (round.getRoundStatus() != null && !"Judging".equalsIgnoreCase(round.getRoundStatus().getStatusName())) {
                    throw new IllegalStateException("This round is not currently in the 'Judging' phase.");
                }
            }
            
            // Layer 3: Submission Score Finalize check
            if (Boolean.TRUE.equals(existingJudging.getSubmission().getIsScoreApproved())) {
                throw new IllegalStateException("The score for this submission has been finalized by the Organizer and cannot be modified.");
            }

            // 5. Validate that the score value does not exceed the maximum allowed value
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
            Event event = resolveEvent(submission);

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
    public List<JudgingDTO> getScoresBySubmissionAndJudgeId(UUID submissionId) {
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new org.springframework.security.access.AccessDeniedException("Actor not found from token");
        }
        
        boolean isOrganizer = actor.getUserType() != null && "Organizer".equalsIgnoreCase(actor.getUserType().getTypeName());
        
        List<Judging> judgings;
        if (isOrganizer) {
            judgings = judgingRepository.findBySubmission_SubmissionIdIn(java.util.Collections.singletonList(submissionId));
        } else {
            judgings = judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(submissionId, actor.getUserId());
        }
        
        return judgings.stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getBatchScoresBySubmissionIds(com.fpt.swp.sealhackathonbe.judging.dto.BatchScoreRequestDTO request) {
        if (request == null || request.getSubmissionIds() == null || request.getSubmissionIds().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Judging> judgings = judgingRepository.findBySubmission_SubmissionIdIn(request.getSubmissionIds());
        return judgings.stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getScoresByJudgeId(UUID roundJudgeId) {
        return judgingRepository.findByRoundJudge_Judge_UserId(roundJudgeId)
                .stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
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
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .collect(Collectors.groupingBy(j -> j.getSubmission().getSubmissionId()));
    }
    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getPublishedScoresBySubmission(UUID submissionId) {
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new org.springframework.security.access.AccessDeniedException("Actor not found from token");
        }

        Submissions submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        // Sample submission (calibration) không thuộc team nào → không có bảng xếp hạng công bố.
        if (submission.getTeam() == null) {
            throw new com.fpt.swp.sealhackathonbe.core.exception.BadRequestException(
                    "Published scores are not available for sample (calibration) submissions.");
        }

        UUID teamId = submission.getTeam().getTeamId();
        UUID roundId = submission.getRoundId();
        UUID categoryId = submission.getTeam().getCategoryId();

        // 1. Verify user is in the team
        boolean isTeamMember = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, actor.getUserId()).isPresent();
        if (!isTeamMember) {
            throw new org.springframework.security.access.AccessDeniedException("You are not a member of this team.");
        }

        // 2. Check if the RoundRanking for this team and round is published
        Optional<RoundRanking> roundRankingOpt = roundRankingRepository
                .findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(roundId, categoryId, teamId);

        if (roundRankingOpt.isEmpty() || !Boolean.TRUE.equals(roundRankingOpt.get().getIsPublished())) {
            throw new org.springframework.security.access.AccessDeniedException("Results are not published yet.");
        }

        // 3. Fetch scores and anonymize judges
        List<Judging> judgings = judgingRepository.findBySubmission_SubmissionIdIn(Collections.singletonList(submissionId));
        
        // Group by judge ID to assign consistent "Judge 1", "Judge 2" numbers
        Map<UUID, String> judgeAnonymizationMap = new HashMap<>();
        int judgeCounter = 1;
        
        List<JudgingDTO> results = new ArrayList<>();
        for (Judging j : judgings) {
            if (!Boolean.TRUE.equals(j.getIsActive())) continue;
            
            UUID judgeUserId = j.getRoundJudge().getJudge().getUserId();
            if (!judgeAnonymizationMap.containsKey(judgeUserId)) {
                judgeAnonymizationMap.put(judgeUserId, "Judge " + judgeCounter++);
            }
            
            JudgingDTO dto = convertToDTO(j);
            dto.setJudgeName(judgeAnonymizationMap.get(judgeUserId)); // Override with anonymous name
            results.add(dto);
        }

        return results;
    }

    private JudgingDTO convertToDTO(Judging judging) {
        return JudgingDTO.builder()
                .id(judging.getId())
                .submissionId(judging.getSubmission() != null ? judging.getSubmission().getSubmissionId() : null)
                .roundJudgeId(judging.getRoundJudge() != null ? judging.getRoundJudge().getJudge().getUserId() : null)
                .judgeName(judging.getRoundJudge() != null ? judging.getRoundJudge().getJudge().getFullName() : null)
                .roundCriterionId(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getRoundCriterionId() : null)
                .criterionName(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getCriterionName() : null)
                .criterionWeight(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getWeight() : null)
                .scoreValue(judging.getScoreValue())
                .comment(judging.getComment())
                .scoredAt(judging.getScoredAt())
                .updatedAt(judging.getUpdatedAt())
                .isCalibration(judging.getIsCalibration())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationAuditLogDTO> getEvaluationAuditLogsByEvent(UUID eventId) {
        return evaluationAuditLogRepository.findByEvent_EventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(log -> EvaluationAuditLogDTO.builder()
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

    @Override
    @Transactional
    public void rejectSubmissionScores(UUID submissionId, String reason) {
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new AccessDeniedException("Actor not found from token");
        }

        Submissions submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        Teams team = submission.getTeam();
        Event event = resolveEvent(submission);

        // Fetch active judging records
        List<Judging> activeJudgings = judgingRepository.findBySubmission_SubmissionId(submissionId)
                .stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .collect(Collectors.toList());

        List<EvaluationAuditLog> auditLogs = new ArrayList<>();

        for (Judging judging : activeJudgings) {
            judging.setIsActive(false);

            EvaluationAuditLog auditLog = new EvaluationAuditLog();
            auditLog.setEvent(event);
            auditLog.setActionType("SCORE_DELETED");
            auditLog.setActor(actor);
            auditLog.setTeam(team);
            auditLog.setSubmission(submission);
            auditLog.setScore(judging);
            auditLog.setReason(reason);
            auditLogs.add(auditLog);
        }

        judgingRepository.saveAll(activeJudgings);
        evaluationAuditLogRepository.saveAll(auditLogs);

        // Reset submission status
        submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
        submission.setIsScoreApproved(false);
        submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public void deleteJudging(UUID submissionId, String reason) {
        Submissions submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with ID: " + submissionId));

        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new AccessDeniedException("Actor not found from token");
        }

        RoundJudge judge = roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(actor.getUserId(), submission.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("RoundJudge entity not found for this round and user."));

        Round round = judge.getRound();
        if (round != null && round.getJudgingDeadline() != null) {
            if (LocalDateTime.now().isAfter(round.getJudgingDeadline())) {
                throw new IllegalStateException("The judging deadline for this round has passed.");
            }
        }

        Teams team = submission.getTeam();
        Event event = resolveEvent(submission);
        if (event == null) {
            throw new IllegalStateException("Could not log evaluation audit because the submission's event context is missing.");
        }

        List<Judging> existingScores = judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(submissionId, actor.getUserId());
        List<Judging> scoresToDelete = existingScores.stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .collect(Collectors.toList());

        if (scoresToDelete.isEmpty()) {
            throw new IllegalStateException("No active scores found for this submission to delete.");
        }

        List<EvaluationAuditLog> auditLogs = new ArrayList<>();
        for (Judging judging : scoresToDelete) {
            judging.setIsActive(false);

            String oldComment = judging.getComment() != null ? judging.getComment().replace("\"", "\\\"") : "";
            String oldValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", judging.getScoreValue(), oldComment);

            EvaluationAuditLog auditLog = new EvaluationAuditLog();
            auditLog.setEvent(event);
            auditLog.setActionType("SCORE_DELETED");
            auditLog.setActor(actor);
            auditLog.setScore(judging);
            auditLog.setTeam(team);
            auditLog.setSubmission(submission);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(null);
            auditLog.setReason(reason != null && !reason.trim().isEmpty() ? reason : "Judge removed their scores");
            auditLogs.add(auditLog);
        }

        judgingRepository.saveAll(scoresToDelete);
        evaluationAuditLogRepository.saveAll(auditLogs);
    }
}
