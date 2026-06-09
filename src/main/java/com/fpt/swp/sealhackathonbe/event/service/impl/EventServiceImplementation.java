package com.fpt.swp.sealhackathonbe.event.service.impl;

import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import com.fpt.swp.sealhackathonbe.event.mapper.EventMapper;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventStatusRepository;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImplementation implements EventService {

    private final EventRepository eventRepository;
    private final EventStatusRepository eventStatusRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse create(CreateEventRequest request) {
        EventStatus eventStatus = eventStatusRepository
                .findById(request.getEventStatusId())
                .orElseThrow(() -> new RuntimeException("Event status not found"));
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
                .createdById(request.getCreatedById())
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
    public EventResponse update(UUID eventId, UpdateEventRequest request) {
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        EventStatus eventStatus = eventStatusRepository
                .findById(request.getEventStatusId())
                .orElseThrow(() -> new RuntimeException("Event status not found"));

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
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return eventMapper.toEventResponse(event);
    }

    @Override
    public void delete(UUID eventId){
        Event event = eventRepository
                .findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if(event.getEventStatus().getEventStatusName().equalsIgnoreCase("ONGOING")){
            throw new RuntimeException("Cannot delete ongoing event");
        }
        event.setIsDeleted(true);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    };

}
