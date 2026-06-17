package com.fpt.swp.sealhackathonbe.criteria.controller;

import com.fpt.swp.sealhackathonbe.criteria.dto.request.ImportCriteriaToEventRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.EventCriterionResponse;
import com.fpt.swp.sealhackathonbe.criteria.service.EventCriterionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/event/criteria")
@RequiredArgsConstructor
public class EventCriterionController {
    private final EventCriterionService eventCriterionService;

    @PostMapping("/import/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<EventCriterionResponse>> importCriteriaToEvent(
            @PathVariable UUID eventId,
            @RequestBody ImportCriteriaToEventRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventCriterionService.importCriteriaToEvent(eventId, request));
    }
}
