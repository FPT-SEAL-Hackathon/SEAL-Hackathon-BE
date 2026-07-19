package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.team.dto.CreateMilestoneRequest;
import com.fpt.swp.sealhackathonbe.team.dto.MilestoneResponse;
import com.fpt.swp.sealhackathonbe.team.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Quản lý mốc tiến độ (milestone) của team — chỉ mentor và expert mới có quyền
 * thao tác.
 */
@Tag(name = "Team Milestones", description = "Mentor APIs for managing team milestones")
@RestController
@RequestMapping("/api/v1/consultation-requests/{requestId}/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final AuthenticationServiceImpl authService;

    @Operation(summary = "List all milestones for a request")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_EXPERT', 'ROLE_ORGANIZER', 'ROLE_FPT_STUDENT', 'ROLE_EXTERNAL_STUDENT')")
    public ResponseEntity<List<MilestoneResponse>> list(@PathVariable UUID requestId) {
        return ResponseEntity.ok(milestoneService.getByRequest(requestId));
    }

    @Operation(summary = "Create a new milestone for a request")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_EXPERT')")
    public ResponseEntity<MilestoneResponse> create(
            @PathVariable UUID requestId,
            @Valid @RequestBody CreateMilestoneRequest request) {
        UUID mentorId = authService.getCurrentUser().getUserId();
        return ResponseEntity.ok(milestoneService.create(requestId, mentorId, request));
    }

    @Operation(summary = "Toggle milestone done/undone")
    @PatchMapping("/{milestoneId}/toggle")
    @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_EXPERT', 'ROLE_FPT_STUDENT', 'ROLE_EXTERNAL_STUDENT')")
    public ResponseEntity<MilestoneResponse> toggle(
            @PathVariable UUID requestId,
            @PathVariable UUID milestoneId) {
        UUID currentUserId = authService.getCurrentUser().getUserId();
        return ResponseEntity.ok(milestoneService.toggle(requestId, milestoneId, currentUserId));
    }

    @Operation(summary = "Delete a milestone")
    @DeleteMapping("/{milestoneId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_EXPERT')")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable UUID requestId,
            @PathVariable UUID milestoneId) {
        UUID mentorId = authService.getCurrentUser().getUserId();
        milestoneService.delete(milestoneId, mentorId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Milestone deleted"));
    }
}
