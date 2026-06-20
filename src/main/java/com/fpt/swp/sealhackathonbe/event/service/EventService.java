package com.fpt.swp.sealhackathonbe.event.service;

import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventStatusRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface EventService {
    EventResponse create(CreateEventRequest request);
    EventResponse update(UUID eventId, UpdateEventRequest request);
    EventResponse getById(UUID eventId);
    List<EventResponse> getAll();
    EventResponse updateStatus(UUID eventId, UpdateEventStatusRequest request);
    void delete(UUID eventId);
}
