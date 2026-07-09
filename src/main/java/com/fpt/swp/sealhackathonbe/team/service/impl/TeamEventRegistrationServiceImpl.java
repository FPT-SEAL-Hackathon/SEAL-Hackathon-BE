package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.constant.UserRoleConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.core.exception.ProfileIncompleteException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantEventResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantUserResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.ParticipantStatus;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.ParticipantStatusRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Đăng ký/duyệt sự kiện theo TEAM. Thuộc module team; chỉ dùng repository
 * của eventparticipant/user (không gọi qua service/controller module khác)
 * để giữ ranh giới module rõ ràng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamEventRegistrationServiceImpl implements TeamEventRegistrationService {

    private static final String REGISTRATION_OPEN_NORMALIZED = "REGISTRATION_OPEN";
    private static final String EVENT_STATUS_DRAFT = "Draft";
    private static final String EVENT_STATUS_CANCELLED = "Cancelled";
    private static final String EVENT_STATUS_COMPLETED = "Completed";
    private static final String EVENT_STATUS_ONGOING = "Ongoing";
    private static final String ACCOUNT_STATUS_ACTIVE = "Active";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_TEMPORARY = "TEMPORARY";
    private static final UUID FPT_STUDENT_ID = UserRoleConstants.ROLE_ADMIN;
    private static final UUID EXTERNAL_STUDENT_ID = UserRoleConstants.ROLE_USER;

    private final EventRepository eventRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final ParticipantStatusRepository participantStatusRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public void assertEligibleStudent(UUID userId) {
        validateStudentCanRegister(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRegistration(UUID eventId, UUID userId) {
        return eventParticipantRepository.existsByEventIdAndUserId(eventId, userId);
    }

    @Override
    @Transactional
    public void removePendingRegistration(UUID eventId, UUID userId) {
        eventParticipantRepository
                .findByEventIdAndUserId(eventId, userId)
                .filter(participant -> isPendingStatus(currentStatusName(participant)))
                .ifPresent(eventParticipantRepository::delete);
    }

    @Override
    @Transactional
    public List<EventParticipantResponse> registerTeam(UUID teamId, UUID currentUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the team leader can register the team for the event.");
        }

        Event event = getRegisterableEvent(team.getEventId());
        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(teamId);

        Integer minTeamSize = event.getMinTeamSize();
        Integer maxTeamSize = event.getMaxTeamSize();
        if (minTeamSize != null && members.size() < minTeamSize) {
            throw new BusinessConflictException(
                    "Team must have at least " + minTeamSize + " members to register.");
        }
        if (maxTeamSize != null && members.size() > maxTeamSize) {
            throw new BusinessConflictException(
                    "Team must have at most " + maxTeamSize + " members to register.");
        }

        ParticipantStatus pendingStatus = getRegistrationPendingStatus();
        LocalDateTime now = LocalDateTime.now();
        List<EventParticipantResponse> responses = new ArrayList<>();

        for (TeamMembers member : members) {
            User memberUser;
            try {
                memberUser = validateStudentCanRegister(member.getUserId());
            } catch (ProfileIncompleteException ex) {
                User incomplete = userRepository.findById(member.getUserId()).orElse(null);
                String who = incomplete != null ? incomplete.getFullName() : member.getUserId().toString();
                throw new ProfileIncompleteException(
                        "Member \"" + who + "\" must complete their profile before the team can register.");
            }

            EventParticipant saved = eventParticipantRepository
                    .findByEventIdAndUserId(event.getEventId(), member.getUserId())
                    .map(existing -> {
                        if (!isPendingStatus(currentStatusName(existing))) {
                            throw new BusinessConflictException(
                                    "Member \"" + memberUser.getFullName()
                                            + "\" registration has already been processed for this event.");
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        EventParticipant participant = new EventParticipant();
                        participant.setEventId(event.getEventId());
                        participant.setUserId(member.getUserId());
                        participant.setParticipantStatusId(pendingStatus.getStatusId());
                        participant.setParticipantStatus(pendingStatus);
                        participant.setAppliedAt(now);
                        EventParticipant created = saveRegistration(participant);
                        writeAuditLog("TEAM_EVENT_REGISTERED", created, team, currentUserId);
                        return created;
                    });

            try {
                notificationService.sendNotification(
                        member.getUserId(),
                        currentUserId,
                        event.getEventId(),
                        "Team Registered For Event",
                        "Your team " + team.getTeamName() + " has been registered for "
                                + event.getEventName() + " and is waiting for organizer approval."
                );
            } catch (Exception ignored) {
                // Đăng ký không được rollback vì lỗi notification.
            }

            responses.add(toRegistrationResponse(saved, event, memberUser, pendingStatus));
        }

        return responses;
    }

    @Override
    @Transactional
    public void withdrawTeamRegistration(UUID teamId, UUID currentUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the team leader can withdraw the team registration.");
        }

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(teamId);
        List<EventParticipant> participants = new ArrayList<>();
        for (TeamMembers member : members) {
            eventParticipantRepository
                    .findByEventIdAndUserId(team.getEventId(), member.getUserId())
                    .ifPresent(participants::add);
        }

        if (participants.isEmpty()) {
            throw new BusinessConflictException("Team has not registered for the event yet.");
        }

        for (EventParticipant participant : participants) {
            if (!isPendingStatus(currentStatusName(participant))) {
                throw new BusinessConflictException(
                        "Team registration can no longer be withdrawn because the organizer already processed it.");
            }
        }

        for (EventParticipant participant : participants) {
            writeAuditLog("TEAM_EVENT_REGISTRATION_WITHDRAWN", participant, team, currentUserId);
            eventParticipantRepository.delete(participant);
        }
    }

    @Override
    @Transactional
    public void applyTeamDecision(UUID teamId, boolean approved, String note, UUID organizerUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        String targetStatus = approved ? STATUS_ACTIVE : STATUS_REJECTED;
        ParticipantStatus pendingStatus = getRegistrationPendingStatus();
        ParticipantStatus newStatus = participantStatusRepository.findByStatusNameIgnoreCase(targetStatus)
                .orElseThrow(() -> new BadRequestException(
                        "Participant status lookup is not configured for " + targetStatus));

        for (TeamMembers member : teamMembersRepository.findByTeamIdAndActiveTrue(teamId)) {
            EventParticipant participant = eventParticipantRepository
                    .findByEventIdAndUserId(team.getEventId(), member.getUserId())
                    .orElseGet(() -> {
                        EventParticipant created = new EventParticipant();
                        created.setEventId(team.getEventId());
                        created.setUserId(member.getUserId());
                        created.setParticipantStatusId(pendingStatus.getStatusId());
                        created.setParticipantStatus(pendingStatus);
                        created.setAppliedAt(LocalDateTime.now());
                        return saveRegistration(created);
                    });

            if (isPendingStatus(currentStatusName(participant))) {
                String oldStatusName = currentStatusName(participant);

                participant.setParticipantStatusId(newStatus.getStatusId());
                participant.setParticipantStatus(newStatus);
                if (STATUS_ACTIVE.equals(targetStatus)) {
                    participant.setApprovedAt(LocalDateTime.now());
                    participant.setApprovedBy(organizerUserId);
                    participant.setRejectedReason(null);
                } else {
                    participant.setApprovedAt(null);
                    participant.setApprovedBy(null);
                    participant.setRejectedReason(trimToNull(note));
                }

                EventParticipant saved = eventParticipantRepository.save(participant);
                writeAuditLog("EVENT_PARTICIPANT_STATUS_UPDATED", saved, team, organizerUserId);

                try {
                    notifyDecision(saved, oldStatusName, targetStatus, organizerUserId);
                } catch (Exception ignored) {
                    // Không rollback quyết định duyệt vì lỗi notification.
                }
            }
        }
    }

    private void notifyDecision(EventParticipant participant, String oldStatusName, String newStatusName, UUID actorUserId) {
        if (newStatusName.equalsIgnoreCase(oldStatusName)) {
            return;
        }
        String eventName = participant.getEvent() != null ? participant.getEvent().getEventName() : "the event";
        String title;
        String body;
        if (STATUS_ACTIVE.equals(newStatusName)) {
            title = "Event Registration Approved";
            body = "Your registration for " + eventName + " has been approved.";
        } else {
            title = "Event Registration Rejected";
            String reason = trimToNull(participant.getRejectedReason());
            body = "Your registration for " + eventName + " has been rejected."
                    + (reason != null ? " " + reason : "");
        }
        notificationService.sendNotification(
                participant.getUserId(), actorUserId, participant.getEventId(), title, body
        );
    }

    private Event getRegisterableEvent(UUID eventId) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        String statusName = event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null;
        if (!REGISTRATION_OPEN_NORMALIZED.equals(normalizeEventStatus(statusName))) {
            throw new BadRequestException(registrationClosedMessage(statusName));
        }

        LocalDateTime registrationEnd = event.getRegistrationEnd();
        if (registrationEnd != null && LocalDateTime.now().isAfter(registrationEnd)) {
            throw new BadRequestException("Registration is closed because the registration deadline has passed.");
        }

        LocalDateTime registrationStart = event.getRegistrationStart();
        if (registrationStart != null && LocalDateTime.now().isBefore(registrationStart)) {
            throw new BadRequestException("Registration has not started yet.");
        }

        return event;
    }

    private String normalizeEventStatus(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return null;
        }
        return statusName.trim().replace("-", "_").replace(" ", "_").toUpperCase();
    }

    private String registrationClosedMessage(String statusName) {
        if (EVENT_STATUS_DRAFT.equalsIgnoreCase(statusName)) {
            return "Registration is not available because this event is still in draft.";
        }
        if (EVENT_STATUS_CANCELLED.equalsIgnoreCase(statusName)) {
            return "Registration is not available because this event has been cancelled.";
        }
        if (EVENT_STATUS_COMPLETED.equalsIgnoreCase(statusName)) {
            return "Registration is not available because this event has already completed.";
        }
        if (EVENT_STATUS_ONGOING.equalsIgnoreCase(statusName)) {
            return "Registration is not available because this event has already started.";
        }
        return "Registration is not available because this event is not open for registration.";
    }

    private EventParticipant saveRegistration(EventParticipant participant) {
        try {
            return eventParticipantRepository.saveAndFlush(participant);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateRegistrationViolation(ex)) {
                throw new BusinessConflictException("You have already registered for this event.");
            }
            throw new BadRequestException(
                    "Event registration could not be saved because related event, user, or participant status data is invalid.");
        }
    }

    private boolean isDuplicateRegistrationViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("uq_eventparticipants_event_user")
                || (normalized.contains("eventparticipants") && normalized.contains("eventid") && normalized.contains("userid"));
    }

    private User validateStudentCanRegister(UUID currentUserId) {
        if (currentUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required to register for an event.");
        }

        User user = userRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "Authentication is required to register for an event."));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AccessDeniedException("Only students can register for events.");
        }

        String accountStatusName = user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null;

        if (STATUS_TEMPORARY.equalsIgnoreCase(accountStatusName)) {
            throw new ProfileIncompleteException("Please complete your profile before registering for an event.");
        }

        if (!ACCOUNT_STATUS_ACTIVE.equalsIgnoreCase(accountStatusName)) {
            throw new AccessDeniedException("Only active verified students can register for events.");
        }

        UUID userTypeId = user.getUserType() != null ? user.getUserType().getUserTypeId() : null;
        if (!FPT_STUDENT_ID.equals(userTypeId) && !EXTERNAL_STUDENT_ID.equals(userTypeId)) {
            throw new AccessDeniedException("Only students can register for events.");
        }

        boolean profileIncomplete =
                (FPT_STUDENT_ID.equals(userTypeId) && isBlank(user.getFptStudentCode()))
                || (EXTERNAL_STUDENT_ID.equals(userTypeId)
                        && (isBlank(user.getExternalStudentCode()) || isBlank(user.getUniversityName())));
        if (profileIncomplete) {
            throw new ProfileIncompleteException("Please complete your profile before registering for an event.");
        }

        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String currentStatusName(EventParticipant participant) {
        return participant.getParticipantStatus() != null ? participant.getParticipantStatus().getStatusName() : null;
    }

    private boolean isPendingStatus(String statusName) {
        return STATUS_PENDING.equalsIgnoreCase(statusName);
    }

    private ParticipantStatus getRegistrationPendingStatus() {
        return participantStatusRepository.findByStatusNameIgnoreCase(STATUS_PENDING)
                .orElseThrow(() -> new BadRequestException(
                        "Participant status lookup is not configured for PENDING. Please seed participant status values: PENDING, ACTIVE, REJECTED."));
    }

    private void writeAuditLog(String actionType, EventParticipant participant, Teams team, UUID actorUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setEntityType("EventParticipants");
        auditLog.setEntityId(participant.getEventParticipantId());
        auditLog.setEntityKey(participant.getEventId() + ":" + participant.getUserId());
        auditLog.setActorUserId(actorUserId);
        auditLog.setNewValueJson(
                "{\"teamId\":\"" + team.getTeamId()
                        + "\",\"eventId\":\"" + participant.getEventId()
                        + "\",\"userId\":\"" + participant.getUserId() + "\"}"
        );
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    private EventParticipantResponse toRegistrationResponse(
            EventParticipant participant, Event event, User user, ParticipantStatus participantStatus
    ) {
        EventParticipantResponse response = new EventParticipantResponse();
        response.setEventParticipantId(participant.getEventParticipantId());
        response.setEventId(event != null ? event.getEventId() : participant.getEventId());
        response.setEventName(event != null ? event.getEventName() : null);
        response.setEventStatus(event != null && event.getEventStatus() != null
                ? event.getEventStatus().getEventStatusName() : null);
        response.setStudentId(user != null ? user.getUserId() : participant.getUserId());
        response.setStudentName(user != null ? user.getFullName() : null);
        response.setStudentEmail(user != null ? user.getEmail() : null);
        response.setUser(toUserResponse(user));
        response.setEvent(toEventResponse(event));
        response.setParticipantStatus(participantStatus != null ? participantStatus.getStatusName() : null);
        response.setAppliedAt(participant.getAppliedAt());
        response.setApprovedAt(participant.getApprovedAt());
        response.setApprovedBy(null);
        response.setRejectedReason(participant.getRejectedReason());
        return response;
    }

    private EventParticipantUserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        EventParticipantUserResponse response = new EventParticipantUserResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setFptStudentCode(user.getFptStudentCode());
        response.setExternalStudentCode(user.getExternalStudentCode());
        response.setUniversityName(user.getUniversityName());
        response.setUserTypeName(user.getUserType() != null ? user.getUserType().getTypeName() : null);
        response.setAccountStatusName(user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null);
        return response;
    }

    private EventParticipantEventResponse toEventResponse(Event event) {
        if (event == null) {
            return null;
        }
        EventParticipantEventResponse response = new EventParticipantEventResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventStatusName(event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null);
        response.setRegistrationStart(event.getRegistrationStart());
        response.setRegistrationEnd(event.getRegistrationEnd());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        return response;
    }
}
