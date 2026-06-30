package com.fpt.swp.sealhackathonbe.event.service.impl;

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
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    public EventResponse create(CreateEventRequest request) {
        EventStatus eventStatus = eventStatusRepository
                .findById(request.getEventStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Event status not found"));

        if (request.getRegistrationStart()!=null && request.getRegistrationEnd()!=null) {
            if(request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
                throw new IllegalArgumentException("Registration start date must be before end date");
            }
        }

        if (request.getEventStartDate()!=null && request.getEventEndDate()!=null) {
            if(request.getEventStartDate().isAfter(request.getEventEndDate())) {
                throw new IllegalArgumentException("Event start date must be before end date");
            }
        }

        if (request.getMinTeamSize() > request.getMaxTeamSize()) {
            throw new IllegalArgumentException("Minimum team size cannot exceed maximum team size");
        }

        if (!eventStatus.getEventStatusName().equalsIgnoreCase("DRAFT") &&
                !eventStatus.getEventStatusName().equalsIgnoreCase("REGISTRATION OPEN")) {
            throw new IllegalStateException("New event must have DRAFT or REGISTRATION OPEN status");
        }

        //Get current user
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("Current user not found");
        }

        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .eventName(request.getEventName())
                .description(request.getDescription())
                .location(request.getLocation())
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

    @Override
    public List<EventResponse> getAll() {
        return eventRepository.findAllByIsDeletedFalse()
                .stream()
                .map(eventMapper::toEventResponse)
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
        return eventMapper.toEventResponse(event);
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

}
