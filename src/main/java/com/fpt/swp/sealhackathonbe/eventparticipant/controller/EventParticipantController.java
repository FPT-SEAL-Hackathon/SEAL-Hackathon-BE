package com.fpt.swp.sealhackathonbe.eventparticipant.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantBulkStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Event Participants", description = "APIs for event registration approval and participant status")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventParticipantController {
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "appliedAt", "appliedAt",
            "approvedAt", "approvedAt",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "status", "status"
    );

    private final EventParticipantService eventParticipantService;
    private final AuthenticationServiceImpl authenticationService;

    @Operation(summary = "Register for an event")
    @PostMapping({"/events/{eventId}/participants/register", "/events/{eventId}/register"})
    public ResponseEntity<EventParticipantResponse> register(@PathVariable UUID eventId) {
        EventParticipantResponse response = eventParticipantService.register(eventId, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View own event registration status")
    @GetMapping({"/events/{eventId}/participants/me", "/events/{eventId}/registration-status"})
    public ResponseEntity<EventParticipantResponse> getOwnStatus(@PathVariable UUID eventId) {
        EventParticipantResponse response = eventParticipantService.getOwnStatus(eventId, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View own event participation statuses")
    @GetMapping("/users/me/event-participations")
    public ResponseEntity<List<EventParticipantResponse>> getOwnParticipations() {
        List<EventParticipantResponse> response = eventParticipantService.getOwnParticipations(currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search event participants")
    @GetMapping("/organizer/event-participants")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Page<EventParticipantResponse>> search(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String university,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appliedAt,desc") String sort
    ) {
        Page<EventParticipantResponse> response = eventParticipantService.search(
                eventId,
                categoryId,
                status,
                keyword,
                university,
                toPageable(page, size, sort)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update one event participant status")
    @PatchMapping({"/organizer/event-participants/{id}/status", "/event-participants/{id}/status"})
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventParticipantResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody EventParticipantStatusUpdateRequest request
    ) {
        EventParticipantResponse response = eventParticipantService.updateStatus(id, request, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bulk update event participant statuses")
    @PatchMapping({"/organizer/event-participants/status", "/event-participants/status"})
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<EventParticipantResponse>> updateStatuses(
            @Valid @RequestBody EventParticipantBulkStatusUpdateRequest request
    ) {
        List<EventParticipantResponse> response = eventParticipantService.updateStatuses(request, currentUserId());
        return ResponseEntity.ok(response);
    }

    private Pageable toPageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        String[] parts = sort.split(",", 2);
        String requestedField = parts.length > 0 ? parts[0].trim() : "appliedAt";
        String field = SORT_FIELDS.getOrDefault(requestedField, "appliedAt");
        Sort.Direction direction = parts.length == 2 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }

    private UUID currentUserId() {
        return authenticationService.getCurrentUser().getUserId();
    }
}
