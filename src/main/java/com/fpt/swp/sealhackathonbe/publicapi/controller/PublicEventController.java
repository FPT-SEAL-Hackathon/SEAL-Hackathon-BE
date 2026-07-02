package com.fpt.swp.sealhackathonbe.publicapi.controller;

import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/api/v1/events")
    public ResponseEntity<List<EventResponse>> getAll() {
        return ResponseEntity.ok(eventService.getAll());
    }

    @GetMapping("/api/v1/public/events")
    public ResponseEntity<List<EventResponse>> getPublicEvents() {
        return ResponseEntity.ok(eventService.getPublicEvents());
    }

    @GetMapping("/api/v1/events/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping("/api/v1/public/events/{id}")
    public ResponseEntity<EventResponse> getPublicEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getPublicEventById(id));
    }
}
