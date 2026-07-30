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
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
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
import com.fpt.swp.sealhackathonbe.judging.dto.CalibrationJudgeStatusResponse;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    private final com.fpt.swp.sealhackathonbe.notification.service.NotificationService notificationService;

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
            enforceJudgingWindow(round);
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

        // IsCalibration PHAI suy ra tu vong dau, KHONG lay tu client.
        // Truoc day lay thang dto.getIsCalibration(): chi can FE gui thieu/sai co (ban cu, cache
        // danh sach round cu, hoac goi API truc tiep) la diem cua giam khao do bien mat khoi
        // Consensus Matrix va file CSV hieu chuan — ca hai deu loc WHERE IsCalibration = 1 —
        // ma khong he bao loi. Vong nao thi co do, client khong duoc quyet dinh.
        boolean calibrationRound = round != null && Boolean.TRUE.equals(round.getIsCalibrationRound());

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
            newJudging.setIsCalibration(calibrationRound);
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
                enforceJudgingWindow(round);
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
            // KHONG cho client sua co calibration khi cap nhat diem: no thuoc ve vong dau, khong
            // thuoc ve lan cham. De client gui len thi mot ban FE cu co the lat nguoc mot diem
            // hieu chuan da luu thanh diem thuong va lam no bien mat khoi ma tran dong thuan.
            // Dong bo lai theo round de sua luon nhung ban ghi da bi ghi sai truoc day.
            if (round != null) {
                existingJudging.setIsCalibration(Boolean.TRUE.equals(round.getIsCalibrationRound()));
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
                        .eventName(log.getEvent() != null ? log.getEvent().getEventName() : null)
                        .actionType(log.getActionType())
                        .actorUserId(log.getActor() != null ? log.getActor().getUserId() : null)
                        .actorName(log.getActor() != null ? log.getActor().getFullName() : null)
                        .judgingId(log.getScore() != null ? log.getScore().getId() : null)
                        .teamId(log.getTeam() != null ? log.getTeam().getTeamId() : null)
                        .teamName(log.getTeam() != null ? log.getTeam().getTeamName() : null)
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
        if (SubmissionStatusConstants.DISQUALIFIED.equals(submission.getSubmissionStatusId())
                || (team != null && (TeamStatusConstants.DISQUALIFIED.equals(team.getTeamStatusId())
                || TeamStatusConstants.WITHDRAWN.equals(team.getTeamStatusId())))) {
            throw new BusinessConflictException(
                    "Disqualified submissions or submissions from inactive teams cannot have scores rejected");
        }

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

    @Override
    @Transactional
    public SubmissionResponse approveScore(UUID submissionId, boolean approve) {
        Submissions submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        validateSubmissionScoreCanBeChanged(submission);

        if (approve) {
            if (Boolean.TRUE.equals(submission.getIsSampleSubmission())) {
                throw new IllegalStateException("Cannot finalize score for a sample calibration submission.");
            }

            UUID roundId = submission.getRoundId();

            // 1. Get all round criteria
            List<RoundCriterion> criteria = roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId);
            if (criteria.isEmpty()) {
                throw new IllegalStateException("Cannot finalize score because no criteria are configured for this round.");
            }

            // 2. Get active judges assigned to this round
            List<RoundJudge> activeRoundJudges = roundJudgeRepository.findActiveByRoundRoundId(roundId);
            if (activeRoundJudges.isEmpty()) {
                throw new IllegalStateException("Cannot finalize score because no active judges are assigned to this round.");
            }

            // 3. Get all active judging records for this submission
            List<Judging> judgings = judgingRepository.findBySubmission_SubmissionId(submissionId).stream()
                    .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                    .toList();

            if (judgings.isEmpty()) {
                throw new IllegalStateException("Cannot finalize score because no scores have been submitted yet.");
            }

            // Group judgings by judge userId
            Map<UUID, List<Judging>> judgeScoresMap = judgings.stream()
                    .filter(j -> j.getRoundJudge() != null && j.getRoundJudge().getJudge() != null)
                    .collect(Collectors.groupingBy(j -> j.getRoundJudge().getJudge().getUserId()));

            boolean atLeastOneJudgeFullyScored = false;
            for (RoundJudge rj : activeRoundJudges) {
                UUID judgeId = rj.getJudge().getUserId();
                List<Judging> scores = judgeScoresMap.get(judgeId);
                if (scores != null && scores.size() >= criteria.size()) {
                    atLeastOneJudgeFullyScored = true;
                    break;
                }
            }

            if (!atLeastOneJudgeFullyScored) {
                throw new IllegalStateException("Cannot finalize score because no judge has fully scored all criteria for this submission.");
            }
        }

        submission.setIsScoreApproved(approve);

        if (approve) {
            submission.setSubmissionStatusId(SubmissionStatusConstants.SCORED);
        } else {
            submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
        }

        submissionRepository.save(submission);
        return SubmissionMapper.toSubmissionResponse(submission);
    }

    private void validateSubmissionScoreCanBeChanged(Submissions submission) {
        if (SubmissionStatusConstants.DISQUALIFIED.equals(submission.getSubmissionStatusId())) {
            throw new BusinessConflictException("Disqualified submissions cannot have scores approved or rejected");
        }

        UUID teamStatusId = submission.getTeam() != null
                ? submission.getTeam().getTeamStatusId()
                : null;
        if (TeamStatusConstants.DISQUALIFIED.equals(teamStatusId) || TeamStatusConstants.WITHDRAWN.equals(teamStatusId)) {
            throw new BusinessConflictException("Submissions from disqualified or withdrawn teams cannot have scores approved or rejected");
        }
    }

    @Override
    @Transactional
    public void rejectSubmissionScoreForJudge(UUID submissionId, UUID judgeId, String reason) {
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new AccessDeniedException("Actor not found from token");
        }

        Submissions submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        Teams team = submission.getTeam();
        validateSubmissionScoreCanBeChanged(submission);

        Event event = resolveEvent(submission);

        List<Judging> activeJudgings = judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(submissionId, judgeId)
                .stream()
                .filter(j -> Boolean.TRUE.equals(j.getIsActive()))
                .collect(Collectors.toList());

        if (activeJudgings.isEmpty()) {
            throw new IllegalStateException("No active scores found for this judge and submission.");
        }

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

        submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
        submission.setIsScoreApproved(false);
        submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public void rejectJudgeScoresInRound(UUID roundId, UUID judgeId, String reason) {
        User actor = authenticationServiceImpl.getCurrentUser();
        if (actor == null) {
            throw new AccessDeniedException("Actor not found from token");
        }

        List<Judging> activeJudgings = judgingRepository.findActiveByRoundIdAndJudgeUserId(roundId, judgeId);
        if (activeJudgings.isEmpty()) {
            return;
        }

        List<EvaluationAuditLog> auditLogs = new ArrayList<>();
        Set<Submissions> submissionsToUpdate = new java.util.HashSet<>();

        for (Judging judging : activeJudgings) {
            judging.setIsActive(false);

            Submissions submission = judging.getSubmission();
            Teams team = submission.getTeam();
            Event event = resolveEvent(submission);

            EvaluationAuditLog auditLog = new EvaluationAuditLog();
            auditLog.setEvent(event);
            auditLog.setActionType("SCORE_DELETED");
            auditLog.setActor(actor);
            auditLog.setTeam(team);
            auditLog.setSubmission(submission);
            auditLog.setScore(judging);
            auditLog.setReason(reason);
            auditLogs.add(auditLog);

            submission.setIsScoreApproved(false);
            submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
            submissionsToUpdate.add(submission);
        }

        judgingRepository.saveAll(activeJudgings);
        evaluationAuditLogRepository.saveAll(auditLogs);
        submissionRepository.saveAll(submissionsToUpdate);
    }

    // ── Vong hieu chuan: theo doi tien do va nhac nho ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CalibrationJudgeStatusResponse> getCalibrationStatus(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found with ID: " + roundId));
        if (!Boolean.TRUE.equals(round.getIsCalibrationRound())) {
            throw new BusinessConflictException("This round is not a calibration round.");
        }

        List<Submissions> samples = sampleSubmissionsOf(roundId);
        int criteriaCount = roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId).size();
        List<UUID> sampleIds = samples.stream().map(Submissions::getSubmissionId).toList();

        // Diem cua MOI giam khao tren cac bai mau, gom mot lan roi chia theo giam khao.
        Map<UUID, List<Judging>> scoresByJudge = sampleIds.isEmpty()
                ? Map.of()
                : judgingRepository.findBySubmission_SubmissionIdIn(sampleIds).stream()
                        .filter(j -> !Boolean.FALSE.equals(j.getIsActive()))
                        .filter(j -> j.getRoundJudge() != null && j.getRoundJudge().getJudge() != null)
                        .collect(Collectors.groupingBy(j -> j.getRoundJudge().getJudge().getUserId()));

        return roundJudgeRepository.findActiveByRoundRoundId(roundId).stream()
                .filter(rj -> rj.getJudge() != null)
                .map(rj -> {
                    User judge = rj.getJudge();
                    List<Judging> scores = scoresByJudge.getOrDefault(judge.getUserId(), List.of());

                    // Mot bai mau chi tinh la xong khi da cham DU moi tieu chi cua vong; cham
                    // do dang van hien ra o scoredCriterionCount de thay tien do that.
                    long completedSamples = criteriaCount == 0 ? 0 : scores.stream()
                            .collect(Collectors.groupingBy(j -> j.getSubmission().getSubmissionId(), Collectors.counting()))
                            .values().stream()
                            .filter(count -> count >= criteriaCount)
                            .count();

                    return CalibrationJudgeStatusResponse.builder()
                            .judgeUserId(judge.getUserId())
                            .judgeName(judge.getFullName())
                            .email(judge.getEmail())
                            .sampleCount(samples.size())
                            .completedSampleCount((int) completedSamples)
                            .scoredCriterionCount(scores.size())
                            .expectedCriterionCount(samples.size() * criteriaCount)
                            .completed(!samples.isEmpty() && completedSamples == samples.size())
                            .lastScoredAt(scores.stream()
                                    .map(Judging::getUpdatedAt)
                                    .filter(Objects::nonNull)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(null))
                            .build();
                })
                .sorted(Comparator.comparing(CalibrationJudgeStatusResponse::isCompleted)
                        .thenComparing(r -> r.getJudgeName() == null ? "" : r.getJudgeName()))
                .toList();
    }

    @Override
    @Transactional
    public int remindPendingCalibrationJudges(UUID roundId, UUID actorUserId) {
        List<CalibrationJudgeStatusResponse> statuses = getCalibrationStatus(roundId);
        List<UUID> pending = statuses.stream()
                .filter(s -> !s.isCompleted())
                .map(CalibrationJudgeStatusResponse::getJudgeUserId)
                .toList();
        if (pending.isEmpty()) {
            return 0;
        }

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found with ID: " + roundId));
        UUID eventId = round.getCategory() != null && round.getCategory().getEvent() != null
                ? round.getCategory().getEvent().getEventId()
                : null;

        String deadline = round.getJudgingDeadline() != null
                ? " Judging deadline: " + round.getJudgingDeadline() + "."
                : "";
        notificationService.sendBroadcastNotification(
                pending,
                actorUserId,
                eventId,
                "Calibration round not completed",
                "Please score the sample submissions of round \"" + round.getRoundName() + "\" so the panel"
                        + " can align on the scoring scale before judging real submissions." + deadline
        );
        return pending.size();
    }

    /**
     * Cua so cham diem cua mot vong.
     *
     * Vong THI THAT: giu nguyen 3 lop kiem tra cu — chua toi startDate, qua judgingDeadline,
     * hoac trang thai khac "Judging" deu bi chan. Rang buoc nay ton tai de dam bao CONG BANG
     * GIUA CAC DOI, khong duoc noi long.
     *
     * Vong HIEU CHUAN: bo ca 3 lop. Vong nay khong sinh ra bat ky ket qua thi nao (doi khong
     * nop bai duoc, khong tinh xep hang, khong cho di tiep, khong trao giai) nen khong co gi
     * de dam bao cong bang — bo gio cham o day chi gay ma sat cho giam khao. startDate/endDate
     * van duoc giu nguyen vi Schedule dung chung de ve tien do du kien.
     *
     * Doi lai co DUNG MOT dieu kien dong: khi cuoc thi da thuc su buoc vao cham. Sau thoi diem
     * do viec hieu chuan khong con y nghia (hieu chuan xong moi di cham, khong ai hieu chuan
     * giua chung). Organizer con mot loi dong thu cong: dat chinh vong hieu chuan sang
     * "Completed" khi muon chot so lieu dong thuan.
     */
    private void enforceJudgingWindow(Round round) {
        if (Boolean.TRUE.equals(round.getIsCalibrationRound())) {
            enforceCalibrationWindow(round);
            return;
        }

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

    private void enforceCalibrationWindow(Round calibrationRound) {
        if (isRoundStatus(calibrationRound, "Completed")) {
            throw new IllegalStateException(
                    "This calibration round has been closed by the organizer and no longer accepts scores.");
        }

        if (calibrationRound.getCategory() == null) {
            return;
        }

        // "Da toi vong cham chinh thuc" = trong cung category co vong thi that dang Judging
        // hoac da Completed.
        boolean competitionJudgingStarted = roundRepository
                .findByCategoryCategoryIdOrderByRoundOrderAsc(calibrationRound.getCategory().getCategoryId())
                .stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsCalibrationRound()))
                .anyMatch(r -> isRoundStatus(r, "Judging") || isRoundStatus(r, "Completed"));

        if (competitionJudgingStarted) {
            throw new IllegalStateException(
                    "Calibration scoring is closed because judging of the competition rounds has already started.");
        }
    }

    private boolean isRoundStatus(Round round, String statusName) {
        return round.getRoundStatus() != null
                && statusName.equalsIgnoreCase(round.getRoundStatus().getStatusName());
    }

    private List<Submissions> sampleSubmissionsOf(UUID roundId) {
        return submissionRepository.findByRoundId(roundId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsSampleSubmission()))
                .toList();
    }

}
