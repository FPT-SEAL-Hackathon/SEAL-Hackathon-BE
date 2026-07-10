package com.fpt.swp.sealhackathonbe.event.controller;

import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.request.UpdateEventStatusRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/event/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping("/event")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.create(request));
    }

    @PutMapping("/event/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @PatchMapping("/event/status/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventStatusRequest request
            ) {
        return ResponseEntity.ok(eventService.updateStatus(id, request));
    }

    @DeleteMapping("/event/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
