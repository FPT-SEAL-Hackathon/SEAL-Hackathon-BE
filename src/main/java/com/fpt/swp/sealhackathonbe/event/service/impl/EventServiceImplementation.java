package com.fpt.swp.sealhackathonbe.event.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.mapper.AuthenticationService;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
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
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TeamsRepository teamsRepository;
    private final CategoryRepository categoryRepository;
    private final RoundRepository roundRepository;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        String eventName = request.getEventName().trim();
        validateEventTimeline(request);

        validateTeamSize(request.getMinTeamSize(), request.getMaxTeamSize());

        EventStatus draftStatus = eventStatusRepository.findByEventStatusName("Draft")
                .orElseThrow(() -> new EntityNotFoundException("Draft status not found"));

        User user = authenticationService.getCurrentUser();

        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .eventName(eventName)
                .description(request.getDescription())
                .location(request.getLocation().trim())
                .bannerImageUrl(request.getBannerImageUrl())
                .eventStatus(draftStatus)
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

    private void validateEventTimeline(CreateEventRequest request) {
        LocalDateTime registrationStart = request.getRegistrationStart();
        LocalDateTime registrationEnd = request.getRegistrationEnd();
        LocalDateTime eventStart = request.getEventStartDate();
        LocalDateTime eventEnd = request.getEventEndDate();

        boolean hasRegistrationDates = registrationStart != null || registrationEnd != null;
        boolean hasEventDates = eventStart != null || eventEnd != null;

        if (hasRegistrationDates) {
            if (registrationStart == null || registrationEnd == null) {
                throw new BadRequestException("Registration start and end must both be provided");
            }
            if (!registrationStart.isBefore(registrationEnd)) {
                throw new BadRequestException("Registration start date must be before registration end date");
            }
        }

        if (hasEventDates) {
            if (eventStart == null || eventEnd == null) {
                throw new BadRequestException("Event start and end must both be provided");
            }
            if (eventStart.isAfter(eventEnd)) {
                throw new BadRequestException("Event start date must be before event end date");
            }
        }

        if (hasRegistrationDates && hasEventDates) {
            if (!registrationEnd.isBefore(eventStart)) {
                throw new BadRequestException("Registration end time must be before the event starts");
            }
        }
    }

    private void validateRequiredFields(Event event) {
        if (event == null) {
            throw new BadRequestException("Event request is required");
        }
        if (event.getEventName() == null || event.getEventName().isBlank()) {
            throw new BadRequestException("Event name must not be empty");
        }
        if (event.getLocation() == null || event.getLocation().isBlank()) {
            throw new BadRequestException("Location is required");
        }
        if (event.getRegistrationStart() == null) {
            throw new BadRequestException("Registration start time is required");
        }
        if (event.getRegistrationEnd() == null) {
            throw new BadRequestException("Registration end time is required");
        }
        if (event.getEventStartDate() == null) {
            throw new BadRequestException("Event start date is required");
        }
        if (event.getEventEndDate() == null) {
            throw new BadRequestException("Event end date is required");
        }
        if (event.getMinTeamSize() == null) {
            throw new BadRequestException("Min team size is required");
        }
        if (event.getMaxTeamSize() == null) {
            throw new BadRequestException("Max team size is required");
        }
    }

    private void validateTeamSize(Integer minTeamSize, Integer maxTeamSize) {
        if (minTeamSize == null && maxTeamSize == null) {
            return;
        }
        if (minTeamSize == null || maxTeamSize == null) {
            throw new BadRequestException("Minimum and maximum team size must both be provided");
        }
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
                .map(event -> {
                    EventResponse response = eventMapper.toEventResponse(event, participationByEventId.get(event.getEventId()));
                    response.setTeamCount((int) teamsRepository.countByEventId(event.getEventId()));
                    response.setRoundCount((int) roundRepository.countByEventId(event.getEventId()));
                    return response;
                })
                .toList();
    }

    @Override
    public List<EventResponse> getPublicEvents() {
        return eventRepository
                .findAllByIsDeletedFalseAndEventStatusEventStatusNameInOrderByEventStartDateAsc(PUBLIC_EVENT_STATUSES)
                .stream()
                .map(event -> {
                    EventResponse response = eventMapper.toEventResponse(event);
                    response.setTeamCount((int) teamsRepository.countByEventId(event.getEventId()));
                    response.setRoundCount((int) roundRepository.countByEventId(event.getEventId()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublicEventById(UUID eventId) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalseAndEventStatusEventStatusNameIn(eventId, PUBLIC_EVENT_STATUSES)
                .orElseThrow(() -> new EntityNotFoundException("Public event not found"));
        EventResponse response = eventMapper.toEventResponse(event);
        response.setTeamCount((int) teamsRepository.countByEventId(event.getEventId()));
        response.setRoundCount((int) roundRepository.countByEventId(event.getEventId()));
        return response;
    }

    @Override
    @Transactional
    public EventResponse update(UUID eventId, UpdateEventRequest request) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

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

        LocalDateTime newEventStart = request.getEventStartDate();
        LocalDateTime oldEventStart = event.getEventStartDate();
        //Check if event start date is provided and modified
        if (newEventStart != null && oldEventStart != null && !newEventStart.isEqual(oldEventStart)) {
            //If it's modified, the new time must not be in the past
            if (newEventStart.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("When updating event start date, the new time must be in present or future");
            }
        }

        if (request.getEventStartDate()!=null && request.getEventEndDate()!=null) {
            if(request.getEventStartDate().isAfter(request.getEventEndDate())) {
                throw new IllegalArgumentException("Event start date must be before end date");
            }
        }

        if (request.getRegistrationEnd()!=null && request.getEventStartDate()!=null) {
            if (request.getRegistrationEnd().isAfter(request.getEventStartDate())) {
                throw new IllegalArgumentException("Registration end date must be on or before event start date");
            }
        }

        if (request.getMinTeamSize() > request.getMaxTeamSize()) {
            throw new IllegalArgumentException("Minimum team size cannot exceed maximum team size");
        }

        event.setEventName(request.getEventName());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setBannerImageUrl(request.getBannerImageUrl());
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
    @Transactional(readOnly = true)
    public EventResponse getById(UUID eventId) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        UUID currentUserId = currentUserIdOrNull();
        EventParticipant participant = currentUserId != null
                ? getCurrentUserParticipation(eventId, currentUserId)
                : null;
        EventResponse response = eventMapper.toEventResponse(event, participant);
        response.setTeamCount((int) teamsRepository.countByEventId(eventId));
        response.setRoundCount((int) roundRepository.countByEventId(eventId));
        return response;
    }

    @Override
    @Transactional
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
    @Transactional
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

    // Quyet dinh hinh dang danh sach event tra ve (staff thay day du, nguoi khac thay ban rut gon).
    // ADMIN cung la staff: chi XEM de phuc vu bao cao, khong tao/sua event.
    private boolean isCurrentUserOrganizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ORGANIZER".equals(authority.getAuthority())
                        || "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsForOrganizer() {
        List<Event> events = eventRepository.findAllByIsDeletedFalse();

        Map<UUID, EventParticipant> participantsByEvent = getCurrentUserParticipationByEventId(events);

        return events.stream()
                .map(event -> {
                    EventResponse response = eventMapper.toEventResponse(
                            event,
                            participantsByEvent.get(event.getEventId())
                    );
                    response.setTeamCount(
                            (int) teamsRepository.countByEventId(event.getEventId())
                    );
                    response.setRoundCount(
                            (int) roundRepository.countByEventId(event.getEventId())
                    );
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime currentTime = LocalDateTime.now();

        EventStatus upcoming = eventStatusRepository.findByEventStatusName("Upcoming")
                .orElseThrow(() -> new EntityNotFoundException("Upcoming status not found"));

        EventStatus registrationOpen = eventStatusRepository.findByEventStatusName("Registration Open")
                .orElseThrow(() -> new EntityNotFoundException("Registration Open status not found"));

        EventStatus registrationClosed = eventStatusRepository.findByEventStatusName("Registration Closed")
                .orElseThrow(() -> new EntityNotFoundException("Registration Closed status not found"));

        EventStatus ongoing = eventStatusRepository.findByEventStatusName("Ongoing")
                .orElseThrow(() -> new EntityNotFoundException("Ongoing status not found"));

        EventStatus completed = eventStatusRepository.findByEventStatusName("Completed")
                .orElseThrow(() -> new EntityNotFoundException("Completed status not found"));

        List<Event> events = eventRepository.findAllByIsDeletedFalse();

        for (Event event : events) {
            String currentStatus = event.getEventStatus().getEventStatusName();

            if (currentStatus.equalsIgnoreCase("Draft")
                    || currentStatus.equalsIgnoreCase("Cancelled")
                    || currentStatus.equalsIgnoreCase("Completed")
            ) {
                continue;
            }

            if (currentTime.isBefore(event.getRegistrationStart())) {
                event.setEventStatus(upcoming);
            } else if (currentTime.isBefore(event.getRegistrationEnd())) {
                event.setEventStatus(registrationOpen);
            } else if (currentTime.isBefore(event.getEventStartDate())) {
                event.setEventStatus(registrationClosed);
            } else if (currentTime.isBefore(event.getEventEndDate())) {
                event.setEventStatus(ongoing);
            } else {
                event.setEventStatus(completed);
            }
        }

        eventRepository.saveAll(events);
    }

    @Override
    @Transactional
    public EventResponse publishEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!event.getEventStatus().getEventStatusName().equalsIgnoreCase("Draft")) {
            throw new IllegalStateException("Only DRAFT event can be published");
        }

        validateRequiredFields(event);

        if (!LocalDateTime.now().isBefore(event.getRegistrationStart())) {
            throw new BadRequestException("Event must be published before registration start");
        }

        List<Category> categories = categoryRepository.findByEventEventId(eventId);
        if (categories.isEmpty()) {
            throw new BadRequestException("Cannot publish event because no category has been created.");
        }

        for (Category category : categories) {
            if (!roundRepository.existsByCategoryCategoryId(category.getCategoryId())) {
                throw new BadRequestException("Category '" + category.getCategoryName() + "' must contain at least one round.");
            }
        }

        EventStatus upcoming = eventStatusRepository.findByEventStatusName("Upcoming")
                .orElseThrow(() -> new EntityNotFoundException("Upcoming status not found"));

        event.setEventStatus(upcoming);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse cancelEvent(UUID eventId) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        String currentStatus = event.getEventStatus().getEventStatusName();

        if (currentStatus.equalsIgnoreCase("Cancelled")) {
            throw new BadRequestException("Event has already been cancelled");
        }

        if (currentStatus.equalsIgnoreCase("Completed")) {
            throw new BadRequestException("Cannot cancel completed event");
        }

        LocalDateTime today = LocalDateTime.now();

        if (!currentStatus.equalsIgnoreCase("Draft")) {
            if (event.getEventStartDate() != null && !today.isBefore(event.getEventStartDate())) {
                throw new BadRequestException("Cannot cancel ongoing event");
            }
        }

        EventStatus cancelledStatus = eventStatusRepository.findByEventStatusName("Cancelled")
                .orElseThrow(() -> new EntityNotFoundException("Cancelled status not found"));

        event.setEventStatus(cancelledStatus);

        return eventMapper.toEventResponse(event);
    }

}
