package com.fpt.swp.sealhackathonbe.eventparticipant.service.impl;

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
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipantServiceImpl implements EventParticipantService {
    private static final String REGISTRATION_OPEN = "Registration Open";
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final UUID FPT_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID EXTERNAL_STUDENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

    private final EventParticipantRepository eventParticipantRepository;
    private final ParticipantStatusRepository participantStatusRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EventParticipantResponse register(UUID eventId, UUID currentUserId) {
        Event event = getRegisterableEvent(eventId);
        validateStudentCanRegister(currentUserId);

        if (eventParticipantRepository.existsByEventIdAndUserId(eventId, currentUserId)) {
            throw new IllegalStateException("User already registered for this event");
        }

        EventParticipant participant = new EventParticipant();
        participant.setEventId(eventId);
        participant.setUserId(currentUserId);
        ParticipantStatus pendingStatus = getStatus(STATUS_PENDING_APPROVAL);
        participant.setParticipantStatusId(pendingStatus.getStatusId());
        participant.setParticipantStatus(pendingStatus);
        participant.setAppliedAt(LocalDateTime.now());

        EventParticipant savedParticipant = eventParticipantRepository.save(participant);
        return toResponse(eventParticipantRepository
                .findByEventParticipantId(savedParticipant.getEventParticipantId())
                .orElse(savedParticipant));
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
            Pageable pageable
    ) {
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
                .search(eventId, categoryId, normalizedStatus, normalizedKeyword, normalizedUniversity, pageable)
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
        applyStatus(participant, request.getStatus(), request.getRejectedReason(), organizerUserId);
        return toResponse(eventParticipantRepository.save(participant));
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

        participants.forEach(participant ->
                applyStatus(participant, request.getStatus(), request.getRejectedReason(), organizerUserId)
        );

        return eventParticipantRepository.saveAll(participants)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void assertActiveParticipant(UUID eventId, UUID userId) {
        if (!eventParticipantRepository.existsByEventIdAndUserIdAndParticipantStatusStatusNameIgnoreCase(
                eventId,
                userId,
                STATUS_ACTIVE
        )) {
            throw new IllegalStateException("User is not an approved participant for this event");
        }
    }

    private Event getRegisterableEvent(UUID eventId) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        String statusName = event.getEventStatus() != null
                ? event.getEventStatus().getEventStatusName()
                : null;
        if (!REGISTRATION_OPEN.equalsIgnoreCase(statusName)) {
            throw new IllegalStateException("Event registration is not open");
        }

        LocalDateTime registrationEnd = event.getRegistrationEnd();
        if (registrationEnd != null && LocalDateTime.now().isAfter(registrationEnd)) {
            throw new IllegalStateException("Registration deadline has passed");
        }

        return event;
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
        String currentStatusName = participant.getParticipantStatus() != null
                ? participant.getParticipantStatus().getStatusName()
                : null;

        if (newStatus.getStatusName().equalsIgnoreCase(currentStatusName)) {
            throw new IllegalStateException("Participant already has this status");
        }

        participant.setParticipantStatusId(newStatus.getStatusId());
        participant.setParticipantStatus(newStatus);
        participant.setRejectedReason(trimToNull(rejectedReason));

        if (STATUS_ACTIVE.equalsIgnoreCase(newStatus.getStatusName())) {
            participant.setApprovedAt(LocalDateTime.now());
            participant.setApprovedBy(organizerUserId);
        } else if (STATUS_REJECTED.equalsIgnoreCase(newStatus.getStatusName())) {
            participant.setApprovedAt(null);
            participant.setApprovedBy(null);
        }
    }

    private EventParticipantResponse toResponse(EventParticipant participant) {
        EventParticipantResponse response = new EventParticipantResponse();
        response.setParticipantId(participant.getEventParticipantId());
        response.setUser(toUserResponse(participant.getUser()));
        response.setEvent(toEventResponse(participant.getEvent()));
        response.setCurrentStatus(participant.getParticipantStatus() != null
                ? participant.getParticipantStatus().getStatusName()
                : null);
        response.setAppliedAt(participant.getAppliedAt());
        response.setApprovedAt(participant.getApprovedAt());
        response.setApprovedBy(toUserResponse(participant.getApprovedByUser()));
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

    private void validateStudentCanRegister(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UUID userTypeId = user.getUserType() != null ? user.getUserType().getUserTypeId() : null;
        if (!FPT_STUDENT_ID.equals(userTypeId) && !EXTERNAL_STUDENT_ID.equals(userTypeId)) {
            throw new IllegalStateException("Only student accounts can register for events");
        }
    }

    private ParticipantStatus getStatus(String statusName) {
        String normalizedStatus = normalizeStatusName(statusName);
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }

        return participantStatusRepository.findByStatusNameIgnoreCase(normalizedStatus)
                .orElseThrow(() -> new IllegalArgumentException("Invalid participant status"));
    }

    private String normalizeStatusName(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return null;
        }
        return statusName.trim().toUpperCase();
    }
}
