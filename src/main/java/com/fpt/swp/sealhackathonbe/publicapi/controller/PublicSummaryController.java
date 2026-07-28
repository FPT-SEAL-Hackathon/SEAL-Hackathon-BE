package com.fpt.swp.sealhackathonbe.publicapi.controller;

import com.fpt.swp.sealhackathonbe.publicapi.dto.LandingSummaryResponse;
import com.fpt.swp.sealhackathonbe.publicapi.service.PublicSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Summary", description = "Aggregate public APIs for Landing Page")
public class PublicSummaryController {

    private final PublicSummaryService publicSummaryService;

    @Operation(summary = "Get landing page summary",
            description = "Returns events, total teams, total prize, and hall of fame in a single request.")
    @GetMapping("/landing-summary")
    public ResponseEntity<LandingSummaryResponse> getLandingSummary() {
        return ResponseEntity.ok(publicSummaryService.getLandingSummary());
    }
}
