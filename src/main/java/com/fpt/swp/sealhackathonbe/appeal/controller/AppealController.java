package com.fpt.swp.sealhackathonbe.appeal.controller;

import com.fpt.swp.sealhackathonbe.appeal.dto.AppealRequestDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResolutionDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResponseDTO;
import com.fpt.swp.sealhackathonbe.appeal.service.AppealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid User ID in token");
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    public ResponseEntity<Map<String, Object>> createAppeal(
            @Valid @RequestBody AppealRequestDTO request,
            Authentication authentication) {
        
        UUID userId = currentUserId(authentication);
        AppealResponseDTO response = appealService.createAppeal(request, userId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", "Appeal submitted successfully");
        body.put("data", response);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PatchMapping("/{appealId}/resolve")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, Object>> resolveAppeal(
            @PathVariable UUID appealId,
            @Valid @RequestBody AppealResolutionDTO resolution,
            Authentication authentication) {
        
        UUID adminId = currentUserId(authentication);
        AppealResponseDTO response = appealService.resolveAppeal(appealId, resolution, adminId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", "Appeal resolved successfully");
        body.put("data", response);
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    public ResponseEntity<Map<String, Object>> getAppealsByTeam(@PathVariable UUID teamId) {
        List<AppealResponseDTO> appeals = appealService.getAppealsByTeam(teamId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("data", appeals);
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, Object>> getAppealsByEvent(@PathVariable UUID eventId) {
        List<AppealResponseDTO> appeals = appealService.getAppealsByEvent(eventId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("data", appeals);
        
        return ResponseEntity.ok(body);
    }
}
