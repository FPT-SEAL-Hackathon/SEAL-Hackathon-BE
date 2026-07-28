package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionHistoryService;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamWithdrawalRequestRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamWithdrawalRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamWithdrawalRequestServiceImpl implements TeamWithdrawalRequestService {
    private static final UUID TEAM_STATUS_ACTIVE = TeamStatusConstants.ACTIVE;
    private static final UUID TEAM_STATUS_WITHDRAWN = TeamStatusConstants.WITHDRAWN;
    private static final UUID SUBMISSION_STATUS_DISQUALIFIED = SubmissionStatusConstants.DISQUALIFIED;

    private static final String REQUEST_STATUS_APPROVED = "APPROVED";
    private static final String WITHDRAWN_SUBMISSION_DISQUALIFICATION_REASON =
            "Team voluntarily withdrew";
    private static final List<String> WITHDRAWAL_SUBMISSION_ROUND_STATUSES =
            List.of("submission open", "judging");

    private final TeamsRepository teamsRepository;
    private final TeamWithdrawalRequestRepository withdrawalRequestRepository;
    private final RoundRepository roundRepository;
    private final SubmissionsRepository submissionsRepository;
    private final SubmissionHistoryService submissionHistoryService;
    private final JudgingRepository judgingRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final AuditLogRepository auditLogRepository;
    private final TeamEventRegistrationService teamEventRegistrationService;

    @Override
    @Transactional
    public TeamWithdrawalRequestResponse requestWithdrawal(
            UUID teamId,
            CreateTeamWithdrawalRequest request,
            UUID currentUserId
    ) {
        Teams team = teamsRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the team leader can request team withdrawal.");
        }

        if (!TEAM_STATUS_ACTIVE.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Only active teams can request withdrawal.");
        }

        LocalDateTime now = LocalDateTime.now();
        TeamWithdrawalRequest withdrawalRequest = new TeamWithdrawalRequest();
        withdrawalRequest.setTeamId(teamId);
        withdrawalRequest.setRequestedById(currentUserId);
        withdrawalRequest.setReason(request.getReason().trim());
        withdrawalRequest.setRequestStatus(REQUEST_STATUS_APPROVED);
        withdrawalRequest.setRequestedAt(now);

        team.setTeamStatusId(TEAM_STATUS_WITHDRAWN);
        team.setUpdatedAt(now);
        teamsRepository.save(team);
        teamEventRegistrationService.markTeamParticipantsWithdrawn(team.getTeamId(), currentUserId);
        disqualifyUnjudgedSubmittedSubmissionsInCurrentRounds(team, currentUserId, now);

        TeamWithdrawalRequest savedRequest = withdrawalRequestRepository.save(withdrawalRequest);
        writeAuditLog("TEAM_WITHDRAWN", savedRequest, currentUserId);
        return toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamWithdrawalRequestResponse> getWithdrawalRequests(UUID eventId) {
        return withdrawalRequestRepository
                .findByTeam_EventIdOrderByRequestedAtDesc(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamWithdrawalRequestResponse> getMyWithdrawalRequests(UUID currentUserId) {
        return withdrawalRequestRepository
                .findByRequestedByIdOrderByRequestedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void disqualifyUnjudgedSubmittedSubmissionsInCurrentRounds(
            Teams team,
            UUID organizerUserId,
            LocalDateTime now
    ) {
        List<UUID> currentRoundIds = roundRepository.findRoundIdsByCategoryIdAndStatusNames(
                team.getCategoryId(),
                WITHDRAWAL_SUBMISSION_ROUND_STATUSES
        );
        if (currentRoundIds.isEmpty()) {
            return;
        }

        List<Submissions> submissions = submissionsRepository.findByTeamIdAndRoundIdIn(
                team.getTeamId(),
                currentRoundIds
        );
        for (Submissions submission : submissions) {
            if (submission.getSubmittedAt() == null
                    || SUBMISSION_STATUS_DISQUALIFIED.equals(submission.getSubmissionStatusId())
                    || judgingRepository.existsBySubmission_SubmissionIdAndIsActiveTrue(submission.getSubmissionId())
                    || hasActiveSubmissionDisqualification(submission.getSubmissionId())) {
                continue;
            }

            submission.setSubmissionStatusId(SUBMISSION_STATUS_DISQUALIFIED);
            submission.setLastUpdatedAt(now);
            submissionsRepository.save(submission);
            submissionHistoryService.recordSnapshot(submission);

            Disqualifications disqualification = new Disqualifications();
            disqualification.setTeamId(null);
            disqualification.setSubmissionId(submission.getSubmissionId());
            disqualification.setReason(WITHDRAWN_SUBMISSION_DISQUALIFICATION_REASON);
            disqualification.setDisqualifiedById(organizerUserId);
            disqualification.setDisqualifiedAt(now);
            disqualification.setReversed(false);
            disqualificationsRepository.save(disqualification);
        }
    }

    private boolean hasActiveSubmissionDisqualification(UUID submissionId) {
        return disqualificationsRepository.findBySubmissionId(submissionId)
                .stream()
                .anyMatch(disqualification -> !Boolean.TRUE.equals(disqualification.getReversed()));
    }

    private TeamWithdrawalRequestResponse toResponse(TeamWithdrawalRequest request) {
        TeamWithdrawalRequestResponse response = new TeamWithdrawalRequestResponse();
        response.setRequestId(request.getRequestId());
        response.setTeamId(request.getTeamId());
        response.setRequestedById(request.getRequestedById());
        response.setReason(request.getReason());
        response.setRequestStatus(request.getRequestStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setRespondedAt(request.getRespondedAt());
        response.setRespondedById(request.getRespondedById());
        response.setResponseNote(request.getResponseNote());

        if (request.getTeam() != null) {
            response.setTeamName(request.getTeam().getTeamName());
            response.setEventId(request.getTeam().getEventId());
        }
        if (request.getRequestedBy() != null) {
            response.setRequestedByName(request.getRequestedBy().getFullName());
            response.setRequestedByEmail(request.getRequestedBy().getEmail());
        }

        return response;
    }

    private void writeAuditLog(String actionType, TeamWithdrawalRequest request, UUID actorUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setEntityType("TeamWithdrawalRequests");
        auditLog.setEntityId(request.getRequestId());
        auditLog.setActorUserId(actorUserId);
        auditLog.setNewValueJson(
                "{\"teamId\":\"" + request.getTeamId()
                        + "\",\"status\":\"" + request.getRequestStatus()
                        + "\"}"
        );
        auditLog.setNotes(request.getReason());
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

}
