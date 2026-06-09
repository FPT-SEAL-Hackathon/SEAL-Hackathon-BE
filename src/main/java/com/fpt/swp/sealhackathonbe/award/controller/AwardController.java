package com.fpt.swp.sealhackathonbe.award.controller;

import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/awards")
@RequiredArgsConstructor
public class AwardController {

    private final AwardService awardService;

    /**
     * API Trao giải thưởng cho một đội thi (Dành cho Event Coordinator / Admin)
     * POST /api/v1/awards
     */
    @PostMapping
    public ResponseEntity<AwardResponse> grantAward(@Valid @RequestBody AwardRequest request) {
//        UUID AdminId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        UUID mockAdminId = UUID.fromString("10000000-0000-0000-0000-000000000003");

        AwardResponse response = awardService.grantAward(request, mockAdminId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API Lấy chi tiết một giải thưởng cụ thể
     * GET /api/v1/awards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AwardResponse> getAwardById(@PathVariable UUID id) {
        AwardResponse response = awardService.getAwardById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * API Lấy toàn bộ danh sách giải thưởng của một sự kiện cụ thể
     * GET /api/v1/awards/events/{eventId}
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<AwardResponse>> getAwardsByEvent(@PathVariable UUID eventId) {
        List<AwardResponse> responses = awardService.getAwardsByEvent(eventId);
        return ResponseEntity.ok(responses);
    }
}