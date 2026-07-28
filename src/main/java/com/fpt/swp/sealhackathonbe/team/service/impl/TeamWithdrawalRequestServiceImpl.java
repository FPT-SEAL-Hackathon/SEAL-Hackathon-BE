package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.HandleTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamWithdrawalRequestRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamWithdrawalRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamWithdrawalRequestServiceImpl implements TeamWithdrawalRequestService {
    private static final UUID TEAM_STATUS_ACTIVE = TeamStatusConstants.ACTIVE;
    private static final UUID TEAM_STATUS_WITHDRAWN = TeamStatusConstants.WITHDRAWN;

    private static final String REQUEST_STATUS_PENDING = "PENDING";
    private static final String REQUEST_STATUS_APPROVED = "APPROVED";
    private static final String REQUEST_STATUS_REJECTED = "REJECTED";

    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamWithdrawalRequestRepository withdrawalRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final TeamEventRegistrationService teamEventRegistrationService;

    @Override
    @Transactional
    public TeamWithdrawalRequestResponse requestWithdrawal(
            UUID teamId,
            CreateTeamWithdrawalRequest request,
            UUID currentUserId
    ) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the team leader can request team withdrawal.");
        }

        if (!TEAM_STATUS_ACTIVE.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Only active teams can request withdrawal.");
        }

        if (withdrawalRequestRepository.existsByTeamIdAndRequestStatus(teamId, REQUEST_STATUS_PENDING)) {
            throw new BusinessConflictException("Team already has a pending withdrawal request.");
        }

        TeamWithdrawalRequest withdrawalRequest = new TeamWithdrawalRequest();
        withdrawalRequest.setTeamId(teamId);
        withdrawalRequest.setRequestedById(currentUserId);
        withdrawalRequest.setReason(request.getReason().trim());
        withdrawalRequest.setRequestStatus(REQUEST_STATUS_PENDING);
        withdrawalRequest.setRequestedAt(LocalDateTime.now());

        TeamWithdrawalRequest savedRequest = withdrawalRequestRepository.save(withdrawalRequest);
        writeAuditLog("TEAM_WITHDRAWAL_REQUESTED", savedRequest, currentUserId);
        return toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamWithdrawalRequestResponse> getPendingWithdrawalRequests(UUID eventId) {
        return withdrawalRequestRepository
                .findByTeam_EventIdAndRequestStatus(eventId, REQUEST_STATUS_PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamWithdrawalRequestResponse> getMyPendingWithdrawalRequests(UUID currentUserId) {
        return withdrawalRequestRepository
                .findByRequestedByIdAndRequestStatus(currentUserId, REQUEST_STATUS_PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TeamWithdrawalRequestResponse handleWithdrawalRequest(
            UUID requestId,
            HandleTeamWithdrawalRequest request,
            UUID organizerUserId
    ) {
        String responseNote = trimToNull(request.getResponseNote());
        if (REQUEST_STATUS_REJECTED.equals(request.getAction()) && responseNote == null) {
            throw new BadRequestException("Response note is required when rejecting a withdrawal request.");
        }

        TeamWithdrawalRequest withdrawalRequest = withdrawalRequestRepository
                .findByRequestIdAndRequestStatus(requestId, REQUEST_STATUS_PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Pending team withdrawal request not found"));

        Teams team = teamsRepository.findByIdForUpdate(withdrawalRequest.getTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        LocalDateTime now = LocalDateTime.now();
        if (REQUEST_STATUS_APPROVED.equals(request.getAction())) {
            if (!TEAM_STATUS_ACTIVE.equals(team.getTeamStatusId())) {
                throw new BusinessConflictException("Only active teams can be withdrawn.");
            }
            team.setTeamStatusId(TEAM_STATUS_WITHDRAWN);
            team.setUpdatedAt(now);
            teamsRepository.save(team);
            teamEventRegistrationService.markTeamParticipantsWithdrawn(team.getTeamId(), organizerUserId);
            withdrawalRequest.setRequestStatus(REQUEST_STATUS_APPROVED);
            writeAuditLog("TEAM_WITHDRAWAL_APPROVED", withdrawalRequest, organizerUserId);
        } else if (REQUEST_STATUS_REJECTED.equals(request.getAction())) {
            withdrawalRequest.setRequestStatus(REQUEST_STATUS_REJECTED);
            writeAuditLog("TEAM_WITHDRAWAL_REJECTED", withdrawalRequest, organizerUserId);
        } else {
            throw new BadRequestException("Invalid request action");
        }

        withdrawalRequest.setRespondedAt(now);
        withdrawalRequest.setRespondedById(organizerUserId);
        withdrawalRequest.setResponseNote(responseNote);

        TeamWithdrawalRequest savedRequest = withdrawalRequestRepository.save(withdrawalRequest);
        notifyTeamWithdrawalDecision(team, savedRequest, organizerUserId);
        return toResponse(savedRequest);
    }

    private void notifyTeamWithdrawalDecision(
            Teams team,
            TeamWithdrawalRequest request,
            UUID organizerUserId
    ) {
        List<UUID> recipientIds = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId())
                .stream()
                .map(TeamMembers::getUserId)
                .distinct()
                .toList();
        if (recipientIds.isEmpty()) {
            return;
        }

        String teamName = team.getTeamName() != null ? team.getTeamName() : "Your team";
        String eventName = team.getEvent() != null && team.getEvent().getEventName() != null
                ? team.getEvent().getEventName()
                : "the event";

        String title;
        String body;
        if (REQUEST_STATUS_APPROVED.equals(request.getRequestStatus())) {
            title = "Team Withdrawal Approved";
            body = teamName + "'s withdrawal request for " + eventName + " has been approved.";
        } else if (REQUEST_STATUS_REJECTED.equals(request.getRequestStatus())) {
            title = "Team Withdrawal Rejected";
            body = teamName + "'s withdrawal request for " + eventName + " has been rejected."
                    + " Reason: " + request.getResponseNote();
        } else {
            return;
        }

        try {
            notificationService.sendBroadcastNotification(
                    recipientIds,
                    organizerUserId,
                    team.getEventId(),
                    title,
                    body
            );
        } catch (Exception ex) {
            log.warn("Failed to send team withdrawal decision notification for team {}: {}",
                    team.getTeamId(), ex.getMessage());
        }
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

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
