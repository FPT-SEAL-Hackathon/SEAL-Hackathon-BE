package com.fpt.swp.sealhackathonbe.award.controller;

import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/awards")
@RequiredArgsConstructor
@Tag(name = "Award Management", description = "APIs for granting and viewing awards")
public class AwardController {

    private final AwardService awardService;

    /**
     * API Trao giải thưởng cho một đội thi (Dành cho Event Coordinator / Admin)
     * POST /api/v1/awards
     */
    @Operation(summary = "Grant award to a team", description = "Create an award for a team in an event.", operationId = "grantAwardToTeam")
    @PostMapping
    public ResponseEntity<AwardResponse> grantAwardToTeam(@Valid @RequestBody AwardRequest request, @AuthenticationPrincipal UserPrincipal principal) {

        AwardResponse response = awardService.grantAward(request, principal.getUser().getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API Lấy chi tiết một giải thưởng cụ thể
     * GET /api/v1/awards/{id}
     */
    @Operation(summary = "Get award details", description = "Get the details of a specific award by its ID.", operationId = "getAwardById")
    @GetMapping("/{id}")
    public ResponseEntity<AwardResponse> getAwardById(@PathVariable UUID id) {
        AwardResponse response = awardService.getAwardById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * API Lấy toàn bộ danh sách giải thưởng của một sự kiện cụ thể
     * GET /api/v1/awards/events/{eventId}
     */
    @Operation(summary = "Get awards by event", description = "Get all awards belonging to a specific event.", operationId = "getAwardsByEvent")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<AwardResponse>> getAwardsByEvent(@PathVariable UUID eventId) {
        List<AwardResponse> responses = awardService.getAwardsByEvent(eventId);
        return ResponseEntity.ok(responses);
    }
}
