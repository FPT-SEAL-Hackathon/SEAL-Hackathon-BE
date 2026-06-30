package com.fpt.swp.sealhackathonbe.eventparticipant.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantBulkStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantEventResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantUserResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.ParticipantStatus;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.ParticipantStatusRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventParticipantServiceImpl implements EventParticipantService {
    private static final String REGISTRATION_OPEN_NORMALIZED = "REGISTRATION_OPEN";
    private static final String EVENT_STATUS_DRAFT = "Draft";
    private static final String EVENT_STATUS_CANCELLED = "Cancelled";
    private static final String EVENT_STATUS_COMPLETED = "Completed";
    private static final String EVENT_STATUS_ONGOING = "Ongoing";
    private static final String ACCOUNT_STATUS_ACTIVE = "Active";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String STATUS_TEMPORARY = "TEMPORARY";
    private static final String STATUS_UNVERIFIED = "UNVERIFIED";
    private static final UUID FPT_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID EXTERNAL_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

    private final EventParticipantRepository eventParticipantRepository;
    private final ParticipantStatusRepository participantStatusRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public EventParticipantResponse register(UUID eventId, UUID currentUserId) {
        log.info("Event registration started: eventId={}, currentUserId={}", eventId, currentUserId);

        User currentUser = validateStudentCanRegister(currentUserId);
        log.info(
                "Event registration user loaded: userId={}, email={}, userType={}, accountStatus={}",
                currentUser.getUserId(),
                currentUser.getEmail(),
                currentUser.getUserType() != null ? currentUser.getUserType().getTypeName() : null,
                currentUser.getAccountStatus() != null ? currentUser.getAccountStatus().getStatusName() : null
        );

        Event event = getRegisterableEvent(eventId);
        log.info(
                "Event registration event loaded: eventId={}, eventName={}, eventStatus={}, registrationStart={}, registrationEnd={}, isDeleted={}",
                event.getEventId(),
                event.getEventName(),
                event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null,
                event.getRegistrationStart(),
                event.getRegistrationEnd(),
                event.getIsDeleted()
        );

        if (eventParticipantRepository.existsByEventIdAndUserId(eventId, currentUser.getUserId())) {
            log.info(
                    "Event registration duplicate found: eventId={}, userId={}",
                    eventId,
                    currentUser.getUserId()
            );
            throw new BusinessConflictException("You have already registered for this event.");
        }
        log.info("Event registration existing participant check passed: eventId={}, userId={}", eventId, currentUser.getUserId());

        ParticipantStatus pendingStatus = getRegistrationPendingStatus();
        log.info(
                "Event registration participant status loaded: statusName={}, statusId={}",
                pendingStatus.getStatusName(),
                pendingStatus.getStatusId()
        );

        EventParticipant participant = new EventParticipant();
        participant.setEventId(eventId);
        participant.setUserId(currentUser.getUserId());
        participant.setParticipantStatusId(pendingStatus.getStatusId());
        participant.setAppliedAt(LocalDateTime.now());

        log.info(
                "Event registration saving participant: eventId={}, userId={}, participantStatusId={}",
                participant.getEventId(),
                participant.getUserId(),
                participant.getParticipantStatusId()
        );
        EventParticipant savedParticipant = saveRegistration(participant);
        log.info("Event registration saved participant: participantId={}", savedParticipant.getEventParticipantId());

        log.info("Event registration mapping response: participantId={}", savedParticipant.getEventParticipantId());
        EventParticipantResponse response = toRegistrationResponse(savedParticipant, event, currentUser, pendingStatus);
        log.info(
                "Event registration mapped response: eventParticipantId={}, eventName={}, eventStatus={}, participantStatus={}",
                response.getEventParticipantId(),
                response.getEventName(),
                response.getEventStatus(),
                response.getParticipantStatus()
        );
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EventParticipantResponse getOwnStatus(UUID eventId, UUID currentUserId) {
        EventParticipant participant = eventParticipantRepository.findByEventIdAndUserId(eventId, currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Registration status not found"));

        return toResponse(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventParticipantResponse> getOwnParticipations(UUID currentUserId) {
        return eventParticipantRepository.findByUserIdOrderByAppliedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventParticipantResponse> search(
            UUID eventId,
            UUID categoryId,
            String status,
            String keyword,
            String university,
            UUID requesterUserId,
            Pageable pageable
    ) {
        getActor(requesterUserId);
        UUID ownerUserId = requesterUserId;
        String normalizedStatus = normalizeStatusName(status);
        if (normalizedStatus != null) {
            getStatus(normalizedStatus);
        }

        String normalizedKeyword = keyword == null || keyword.trim().isEmpty()
                ? null
                : keyword.trim();
        String normalizedUniversity = university == null || university.trim().isEmpty()
                ? null
                : university.trim();

        return eventParticipantRepository
                .search(eventId, categoryId, ownerUserId, normalizedStatus, normalizedKeyword, normalizedUniversity, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public EventParticipantResponse updateStatus(
            UUID participantId,
            EventParticipantStatusUpdateRequest request,
            UUID organizerUserId
    ) {
        EventParticipant participant = getParticipant(participantId);
        User actor = getActor(organizerUserId);
        validateCanUpdateParticipant(participant, actor);

        String oldStatusName = currentStatusName(participant);
        applyStatus(participant, request.getStatus(), request.getRejectedReason(), organizerUserId);

        EventParticipant savedParticipant = eventParticipantRepository.save(participant);
        writeStatusAuditLog(savedParticipant, oldStatusName, currentStatusName(savedParticipant), organizerUserId);
        notifyParticipantAfterStatusChange(savedParticipant, oldStatusName, currentStatusName(savedParticipant), organizerUserId);

        return toResponse(savedParticipant);
    }

    @Override
    @Transactional
    public List<EventParticipantResponse> updateStatuses(
            EventParticipantBulkStatusUpdateRequest request,
            UUID organizerUserId
    ) {
        List<EventParticipant> participants = eventParticipantRepository.findAllById(request.getParticipantIds());
        if (participants.size() != request.getParticipantIds().size()) {
            throw new EntityNotFoundException("One or more participants were not found");
        }

        User actor = getActor(organizerUserId);
        participants.forEach(participant -> validateCanUpdateParticipant(participant, actor));

        List<String> oldStatuses = participants.stream()
                .map(this::currentStatusName)
                .toList();

        participants.forEach(participant ->
                applyStatus(participant, request.getStatus(), request.getRejectedReason(), organizerUserId)
        );

        List<EventParticipant> savedParticipants = eventParticipantRepository.saveAll(participants);
        for (int index = 0; index < savedParticipants.size(); index++) {
            EventParticipant savedParticipant = savedParticipants.get(index);
            String oldStatusName = oldStatuses.get(index);
            String newStatusName = currentStatusName(savedParticipant);
            writeStatusAuditLog(savedParticipant, oldStatusName, newStatusName, organizerUserId);
            notifyParticipantAfterStatusChange(savedParticipant, oldStatusName, newStatusName, organizerUserId);
        }

        return savedParticipants.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void assertActiveParticipant(UUID eventId, UUID userId) {
        EventParticipant participant = eventParticipantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new AccessDeniedException("Register for this event first"));

        if (!isApprovedParticipantStatus(currentStatusName(participant))) {
            throw new org.springframework.security.access.AccessDeniedException("User is not an approved participant for this event");
        }
    }

    private Event getRegisterableEvent(UUID eventId) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        String statusName = event.getEventStatus() != null
                ? event.getEventStatus().getEventStatusName()
                : null;
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
        return statusName.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();
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
            throw new BadRequestException("Event registration could not be saved because related event, user, or participant status data is invalid.");
        }
    }

    private boolean isDuplicateRegistrationViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("uq_eventparticipants_event_user")
                || (normalized.contains("eventparticipants")
                && normalized.contains("eventid")
                && normalized.contains("userid"));
    }

    private EventParticipant getParticipant(UUID participantId) {
        return eventParticipantRepository.findByEventParticipantId(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));
    }

    private void applyStatus(
            EventParticipant participant,
            String requestedStatus,
            String rejectedReason,
            UUID organizerUserId
    ) {
        ParticipantStatus newStatus = getStatus(requestedStatus);
        String currentStatusName = currentStatusName(participant);

        if (newStatus.getStatusName().equalsIgnoreCase(currentStatusName)) {
            throw new BusinessConflictException("Participant already has this status");
        }

        validateStatusTransition(currentStatusName, newStatus.getStatusName());

        participant.setParticipantStatusId(newStatus.getStatusId());
        participant.setParticipantStatus(newStatus);

        if (STATUS_ACTIVE.equalsIgnoreCase(newStatus.getStatusName())) {
            participant.setApprovedAt(LocalDateTime.now());
            participant.setApprovedBy(organizerUserId);
            participant.setRejectedReason(null);
        } else if (STATUS_REJECTED.equalsIgnoreCase(newStatus.getStatusName())) {
            participant.setApprovedAt(null);
            participant.setApprovedBy(null);
            participant.setRejectedReason(trimToNull(rejectedReason));
        } else if (isPendingStatus(newStatus.getStatusName())) {
            participant.setApprovedAt(null);
            participant.setApprovedBy(null);
            participant.setRejectedReason(null);
        }
    }

    private EventParticipantResponse toResponse(EventParticipant participant) {
        String participantStatus = participant.getParticipantStatus() != null
                ? toApiParticipantStatus(participant.getParticipantStatus().getStatusName())
                : null;
        Event event = participant.getEvent();
        User user = participant.getUser();

        EventParticipantResponse response = new EventParticipantResponse();
        response.setEventParticipantId(participant.getEventParticipantId());
        response.setEventId(participant.getEventId());
        response.setEventName(event != null ? event.getEventName() : null);
        response.setEventStatus(event != null && event.getEventStatus() != null
                ? event.getEventStatus().getEventStatusName()
                : null);
        response.setStudentId(participant.getUserId());
        response.setStudentName(user != null ? user.getFullName() : null);
        response.setStudentEmail(user != null ? user.getEmail() : null);
        response.setUser(toUserResponse(user));
        response.setEvent(toEventResponse(event));
        response.setParticipantStatus(participantStatus);
        response.setAppliedAt(participant.getAppliedAt());
        response.setApprovedAt(participant.getApprovedAt());
        response.setApprovedBy(toUserResponse(participant.getApprovedByUser()));
        response.setRejectedReason(participant.getRejectedReason());
        return response;
    }

    private EventParticipantResponse toRegistrationResponse(
            EventParticipant participant,
            Event event,
            User user,
            ParticipantStatus participantStatus
    ) {
        String statusName = participantStatus != null
                ? toApiParticipantStatus(participantStatus.getStatusName())
                : null;

        EventParticipantResponse response = new EventParticipantResponse();
        response.setEventParticipantId(participant.getEventParticipantId());
        response.setEventId(event != null ? event.getEventId() : participant.getEventId());
        response.setEventName(event != null ? event.getEventName() : null);
        response.setEventStatus(event != null && event.getEventStatus() != null
                ? event.getEventStatus().getEventStatusName()
                : null);
        response.setStudentId(user != null ? user.getUserId() : participant.getUserId());
        response.setStudentName(user != null ? user.getFullName() : null);
        response.setStudentEmail(user != null ? user.getEmail() : null);
        response.setUser(toUserResponse(user));
        response.setEvent(toEventResponse(event));
        response.setParticipantStatus(statusName);
        response.setAppliedAt(participant.getAppliedAt());
        response.setApprovedAt(participant.getApprovedAt());
        response.setApprovedBy(null);
        response.setRejectedReason(participant.getRejectedReason());
        return response;
    }

    private String toApiParticipantStatus(String statusName) {
        return statusName;
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
        response.setAccountStatusName(
                user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null
        );
        return response;
    }

    private EventParticipantEventResponse toEventResponse(Event event) {
        if (event == null) {
            return null;
        }

        EventParticipantEventResponse response = new EventParticipantEventResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventStatusName(
                event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null
        );
        response.setRegistrationStart(event.getRegistrationStart());
        response.setRegistrationEnd(event.getRegistrationEnd());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        return response;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private User validateStudentCanRegister(UUID currentUserId) {
        if (currentUserId == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authentication is required to register for an event."
            );
        }

        User user = userRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "Authentication is required to register for an event."
                ));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AccessDeniedException("Only students can register for events.");
        }

        String accountStatusName = user.getAccountStatus() != null
                ? user.getAccountStatus().getStatusName()
                : null;
        if (!ACCOUNT_STATUS_ACTIVE.equalsIgnoreCase(accountStatusName)) {
            throw new AccessDeniedException("Only active verified students can register for events.");
        }

        UUID userTypeId = user.getUserType() != null ? user.getUserType().getUserTypeId() : null;
        if (!FPT_STUDENT_ID.equals(userTypeId) && !EXTERNAL_STUDENT_ID.equals(userTypeId)) {
            throw new AccessDeniedException("Only students can register for events.");
        }

        return user;
    }

    private ParticipantStatus getStatus(String statusName) {
        String normalizedStatus = normalizeStatusName(statusName);
        if (normalizedStatus == null) {
            throw new BadRequestException("Status is required");
        }
        if ("INVALID".equals(normalizedStatus)) {
            throw new BadRequestException("Invalid participant status. Allowed values are: PENDING, ACTIVE, REJECTED.");
        }
        if (isPendingStatus(normalizedStatus)) {
            return getRegistrationPendingStatus();
        }

        return participantStatusRepository.findByStatusNameIgnoreCase(normalizedStatus)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid participant status. Allowed values are: PENDING, ACTIVE, REJECTED."
                ));
    }

    private String normalizeStatusName(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return null;
        }
        String normalized = statusName.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();
        if (STATUS_PENDING.equals(normalized)
                || STATUS_ACTIVE.equals(normalized)
                || STATUS_REJECTED.equals(normalized)) {
            return normalized;
        }
        return "INVALID";
    }

    private User getActor(UUID actorUserId) {
        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private void validateCanUpdateParticipant(EventParticipant participant, User actor) {
        if (participant.getUserId().equals(actor.getUserId())) {
            throw new AccessDeniedException("Users cannot update their own participant status");
        }

        Event event = participant.getEvent();
        UUID ownerUserId = event != null && event.getCreatedBy() != null
                ? event.getCreatedBy().getUserId()
                : null;
        if (!actor.getUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("You do not have permission to update participants for this event");
        }
    }

    private String currentStatusName(EventParticipant participant) {
        return participant.getParticipantStatus() != null
                ? participant.getParticipantStatus().getStatusName()
                : null;
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null) {
            throw new BusinessConflictException("Participant current status is missing");
        }

        boolean allowed =
                (isPendingStatus(currentStatus)
                        && (STATUS_ACTIVE.equalsIgnoreCase(newStatus) || STATUS_REJECTED.equalsIgnoreCase(newStatus)))
                || (STATUS_ACTIVE.equalsIgnoreCase(currentStatus)
                        && (STATUS_SUSPENDED.equalsIgnoreCase(newStatus) || STATUS_TEMPORARY.equalsIgnoreCase(newStatus)))
                || (STATUS_SUSPENDED.equalsIgnoreCase(currentStatus)
                        && STATUS_ACTIVE.equalsIgnoreCase(newStatus))
                || (STATUS_TEMPORARY.equalsIgnoreCase(currentStatus)
                        && STATUS_ACTIVE.equalsIgnoreCase(newStatus))
                || (STATUS_REJECTED.equalsIgnoreCase(currentStatus)
                        && isPendingStatus(newStatus));

        if (!allowed || STATUS_UNVERIFIED.equalsIgnoreCase(newStatus)) {
            throw new BusinessConflictException("Participant status transition is not allowed");
        }
    }

    private boolean isApprovedParticipantStatus(String statusName) {
        return STATUS_ACTIVE.equalsIgnoreCase(statusName);
    }

    private boolean isPendingStatus(String statusName) {
        return STATUS_PENDING.equalsIgnoreCase(statusName);
    }

    private ParticipantStatus getRegistrationPendingStatus() {
        log.info("Event registration resolving participant status lookup: statusName={}", STATUS_PENDING);
        return participantStatusRepository.findByStatusNameIgnoreCase(STATUS_PENDING)
                .orElseThrow(() -> new BadRequestException(
                        "Participant status lookup is not configured for PENDING. Please seed participant status values: PENDING, ACTIVE, REJECTED."
                ));
    }

    private void notifyParticipantAfterStatusChange(
            EventParticipant participant,
            String oldStatusName,
            String newStatusName,
            UUID actorUserId
    ) {
        if (newStatusName == null || newStatusName.equalsIgnoreCase(oldStatusName)) {
            return;
        }

        String eventName = participant.getEvent() != null ? participant.getEvent().getEventName() : "the event";
        String title;
        String body;

        if (STATUS_ACTIVE.equalsIgnoreCase(newStatusName)) {
            title = "Event Registration Approved";
            body = "Your registration for " + eventName
                    + " has been approved. You can now participate and create or join a team.";
        } else if (STATUS_REJECTED.equalsIgnoreCase(newStatusName)) {
            title = "Event Registration Rejected";
            String reason = trimToNull(participant.getRejectedReason());
            body = "Your registration for " + eventName + " has been rejected."
                    + (reason != null ? " " + reason : "");
        } else {
            return;
        }

        try {
            notificationService.sendNotification(
                    participant.getUserId(),
                    actorUserId,
                    participant.getEventId(),
                    title,
                    body
            );
        } catch (Exception ignored) {
            // Participant status changes must not be rolled back by email/realtime notification failures.
        }
    }

    private void writeStatusAuditLog(
            EventParticipant participant,
            String oldStatusName,
            String newStatusName,
            UUID actorUserId
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType("EVENT_PARTICIPANT_STATUS_UPDATED");
        auditLog.setEntityType("EventParticipants");
        auditLog.setEntityId(participant.getEventParticipantId());
        auditLog.setEntityKey(participant.getEventId() + ":" + participant.getUserId());
        auditLog.setActorUserId(actorUserId);
        auditLog.setOldValueJson("{\"status\":\"" + escapeJson(oldStatusName) + "\"}");
        auditLog.setNewValueJson(
                "{\"status\":\"" + escapeJson(newStatusName)
                        + "\",\"eventId\":\"" + participant.getEventId()
                        + "\",\"userId\":\"" + participant.getUserId()
                        + "\",\"rejectedReason\":\"" + escapeJson(participant.getRejectedReason()) + "\"}"
        );
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLog.setNotes(participant.getRejectedReason());

        auditLogRepository.save(auditLog);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
