package com.fpt.swp.sealhackathonbe.event.service.impl;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventStatusRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import com.fpt.swp.sealhackathonbe.event.mapper.EventMapper;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventStatusRepository;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImplementation implements EventService {

    private static final List<String> PUBLIC_EVENT_STATUSES = List.of(
            "Registration Open",
            "Ongoing",
            "Completed"
    );

    private final EventRepository eventRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final EventParticipantRepository eventParticipantRepository;

    @Override
    public EventResponse create(CreateEventRequest request) {
        validateRequiredCreateFields(request);
        String eventName = request.getEventName().trim();

        EventStatus eventStatus = eventStatusRepository
                .findById(request.getEventStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Event status not found"));

        validateEventTimeline(
                request.getRegistrationStart(),
                request.getRegistrationEnd(),
                request.getEventStartDate(),
                request.getEventEndDate()
        );

        validateTeamSize(request.getMinTeamSize(), request.getMaxTeamSize());

        if (eventRepository.existsByEventNameIgnoreCaseAndIsDeletedFalse(eventName)) {
            throw new BadRequestException("Event name already exists");
        }

        String statusName = eventStatus.getEventStatusName();
        if (statusName == null
                || (!statusName.equalsIgnoreCase("DRAFT")
                && !statusName.equalsIgnoreCase("REGISTRATION OPEN"))) {
            throw new BadRequestException("New event must have DRAFT or REGISTRATION OPEN status");
        }

        //Get current user
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("User is not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("Current user not found");
        }

        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .eventName(eventName)
                .description(request.getDescription())
                .location(request.getLocation().trim())
                .bannerImageUrl(request.getBannerImageUrl())
                .eventStatus(eventStatus)
                .registrationStart(request.getRegistrationStart())
                .registrationEnd(request.getRegistrationEnd())
                .eventStartDate(request.getEventStartDate())
                .eventEndDate(request.getEventEndDate())
                .maxTeamSize(request.getMaxTeamSize())
                .minTeamSize(request.getMinTeamSize())
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    private void validateRequiredCreateFields(CreateEventRequest request) {
        if (request == null) {
            throw new BadRequestException("Event request is required");
        }
        if (request.getEventName() == null || request.getEventName().isBlank()) {
            throw new BadRequestException("Event name must not be empty");
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new BadRequestException("Location is required");
        }
        if (request.getEventStatusId() == null) {
            throw new BadRequestException("Event status is required");
        }
        if (request.getRegistrationStart() == null) {
            throw new BadRequestException("Registration start time is required");
        }
        if (request.getRegistrationEnd() == null) {
            throw new BadRequestException("Registration end time is required");
        }
        if (request.getEventStartDate() == null) {
            throw new BadRequestException("Event start date is required");
        }
        if (request.getEventEndDate() == null) {
            throw new BadRequestException("Event end date is required");
        }
        if (request.getMinTeamSize() == null) {
            throw new BadRequestException("Min team size is required");
        }
        if (request.getMaxTeamSize() == null) {
            throw new BadRequestException("Max team size is required");
        }
    }

    private void validateEventTimeline(
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            LocalDate eventStartDate,
            LocalDate eventEndDate
    ) {
        if (registrationStart.isAfter(registrationEnd)) {
            throw new BadRequestException("Registration start time must be before or equal to registration end time");
        }

        if (eventStartDate.isAfter(eventEndDate)) {
            throw new BadRequestException("Event start date must be before or equal to event end date");
        }

        if (registrationEnd.toLocalDate().isAfter(eventStartDate)) {
            throw new BadRequestException("Registration end date must be on or before event start date");
        }
    }

    private void validateTeamSize(Integer minTeamSize, Integer maxTeamSize) {
        if (minTeamSize > maxTeamSize) {
            throw new BadRequestException("Min team size cannot be greater than max team size");
        }
    }

    @Override
    public List<EventResponse> getAll() {
        List<Event> events = isCurrentUserOrganizer()
                ? eventRepository.findAllByIsDeletedFalse()
                : eventRepository.findAllByIsDeletedFalseAndEventStatusEventStatusNameInOrderByEventStartDateAsc(PUBLIC_EVENT_STATUSES);
        Map<UUID, EventParticipant> participationByEventId = getCurrentUserParticipationByEventId(events);

        return events.stream()
                .map(event -> eventMapper.toEventResponse(event, participationByEventId.get(event.getEventId())))
                .toList();
    }

    @Override
    public List<EventResponse> getPublicEvents() {
        return eventRepository
                .findAllByIsDeletedFalseAndEventStatusEventStatusNameInOrderByEventStartDateAsc(PUBLIC_EVENT_STATUSES)
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    @Override
    public EventResponse getPublicEventById(UUID eventId) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalseAndEventStatusEventStatusNameIn(eventId, PUBLIC_EVENT_STATUSES)
                .orElseThrow(() -> new EntityNotFoundException("Public event not found"));
        return eventMapper.toEventResponse(event);
    }

    @Override
    public EventResponse update(UUID eventId, UpdateEventRequest request) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        EventStatus eventStatus = eventStatusRepository
                .findById(request.getEventStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Event status not found"));

        LocalDateTime newRegistrationStart = request.getRegistrationStart();
        LocalDateTime oldRegistrationStart = event.getRegistrationStart();
        //Check if registration start is provided and modified
        if (newRegistrationStart != null && oldRegistrationStart != null && !newRegistrationStart.isEqual(oldRegistrationStart)) {
            //If it's modified, the new time must not be in the past
            if (newRegistrationStart.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("When updating registration start date, the new time must be in the present or future");
            }
        }
        if (request.getRegistrationStart()!=null && request.getRegistrationEnd()!=null) {
            if(request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
                throw new IllegalArgumentException("Registration start date must be before end date");
            }
        }

        LocalDate newEventStart = request.getEventStartDate();
        LocalDate oldEventStart = event.getEventStartDate();
        //Check if event start date is provided and modified
        if (newEventStart != null && oldEventStart != null && !newEventStart.isEqual(oldEventStart)) {
            //If it's modified, the new time must not be in the past
            if (newEventStart.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("When updating event start date, the new time must be in present or future");
            }
        }

        if (request.getEventStartDate()!=null && request.getEventEndDate()!=null) {
            if(request.getEventStartDate().isAfter(request.getEventEndDate())) {
                throw new IllegalArgumentException("Event start date must be before end date");
            }
        }

        if (request.getRegistrationEnd()!=null && request.getEventStartDate()!=null) {
            if (request.getRegistrationEnd().isAfter(request.getEventStartDate().atStartOfDay())) {
                throw new IllegalArgumentException("Registration end date must be on or before event start date");
            }
        }

        if (eventRepository.existsByEventNameIgnoreCaseAndIsDeletedFalseAndEventIdNot(request.getEventName(), eventId)) {
            throw new IllegalArgumentException("Event name already exists");
        }

        if (request.getMinTeamSize() > request.getMaxTeamSize()) {
            throw new IllegalArgumentException("Minimum team size cannot exceed maximum team size");
        }

        event.setEventName(request.getEventName());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setEventStatus(eventStatus);
        event.setRegistrationStart(request.getRegistrationStart());
        event.setRegistrationEnd(request.getRegistrationEnd());
        event.setEventStartDate(request.getEventStartDate());
        event.setEventEndDate(request.getEventEndDate());
        event.setMaxTeamSize(request.getMaxTeamSize());
        event.setMinTeamSize(request.getMinTeamSize());
        event.setUpdatedAt(LocalDateTime.now());

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse getById(UUID eventId) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        UUID currentUserId = currentUserIdOrNull();
        EventParticipant participant = currentUserId != null
                ? getCurrentUserParticipation(eventId, currentUserId)
                : null;
        return eventMapper.toEventResponse(event, participant);
    }

    @Override
    public EventResponse updateStatus(UUID eventId, UpdateEventStatusRequest request) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (event.getEventStatus().getEventStatusName().equalsIgnoreCase("COMPLETED")) {
            throw new IllegalStateException("Cannot change status because event has already been completed");
        }
        EventStatus newStatus = eventStatusRepository.findById(request.getEventStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Event status not found"));

        event.setEventStatus(newStatus);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public void delete(UUID eventId){
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if(event.getEventStatus().getEventStatusName().equalsIgnoreCase("ONGOING")){
            throw new RuntimeException("Cannot delete ongoing event");
        }
        event.setIsDeleted(true);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    };

    private Map<UUID, EventParticipant> getCurrentUserParticipationByEventId(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        UUID currentUserId = currentUserIdOrNull();
        if (currentUserId == null) {
            return Map.of();
        }

        List<UUID> eventIds = events.stream()
                .map(Event::getEventId)
                .toList();

        return eventParticipantRepository.findByUserIdAndEventIdIn(currentUserId, eventIds)
                .stream()
                .collect(Collectors.toMap(EventParticipant::getEventId, Function.identity()));
    }

    private EventParticipant getCurrentUserParticipation(UUID eventId, UUID currentUserId) {
        return eventParticipantRepository.findByEventIdAndUserId(eventId, currentUserId)
                .orElse(null);
    }

    private UUID currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        User user = userRepository.findByEmail(authentication.getName());
        return user != null ? user.getUserId() : null;
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new EntityNotFoundException("Current user not found");
        }

        return user.getUserId();
    }

    private boolean isCurrentUserOrganizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ORGANIZER".equals(authority.getAuthority()));
    }

}
