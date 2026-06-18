package com.fpt.swp.sealhackathonbe.award.controller;

import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternResponse;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.dto.RankingAwardCandidateResponse;
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
     * API for granting an award to a team (for Event Coordinator / Admin)
     * POST /api/v1/awards
     */
    @Operation(summary = "Grant award to a team", description = "Create an award for a team in an event.", operationId = "grantAwardToTeam")
    @PostMapping("/grandAwardToATeam")
    public ResponseEntity<AwardResponse> grantAwardToTeam(@Valid @RequestBody AwardRequest request, @AuthenticationPrincipal UserPrincipal principal) {

        AwardResponse response = awardService.grantAward(request, principal.getUser().getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * API for getting the details of a specific award
     * GET /api/v1/awards/{id}
     */
    @Operation(summary = "Get award details", description = "Get the details of a specific award by its ID.", operationId = "getAwardById")
    @GetMapping("/{id}")
    public ResponseEntity<AwardResponse> getAwardById(@PathVariable UUID id) {
        AwardResponse response = awardService.getAwardById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * API for getting all awards of a specific event
     * GET /api/v1/awards/events/{eventId}
     */
    @Operation(summary = "Get awards by event", description = "Get all awards belonging to a specific event.", operationId = "getAwardsByEvent")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<AwardResponse>> getAwardsByEvent(@PathVariable UUID eventId) {
        List<AwardResponse> responses = awardService.getAwardsByEvent(eventId);
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Create or update top 10 award pattern",
            description = "Configure award title, tier, description, and prize for ranks 1-10 in a category.",
            operationId = "saveTop10AwardPattern"
    )
    @PostMapping("/categories/{categoryId}/top10-pattern")
    public ResponseEntity<List<AwardPatternResponse>> saveTop10Pattern(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AwardPatternRequest request
    ) {
        return ResponseEntity.ok(awardService.saveTop10Pattern(categoryId, request));
    }

    @Operation(
            summary = "Get top 10 award pattern",
            description = "Get active award pattern for ranks 1-10 in a category.",
            operationId = "getTop10AwardPattern"
    )
    @GetMapping("/categories/{categoryId}/top10-pattern")
    public ResponseEntity<List<AwardPatternResponse>> getTop10Pattern(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(awardService.getTop10Pattern(categoryId));
    }

    @Operation(
            summary = "Get top 10 ranking by category",
            description = "Get top 10 teams from RoundRankings by category. If roundId is omitted, the latest round of the category is used.",
            operationId = "getTop10RankingByCategory"
    )
    @GetMapping("/categories/{categoryId}/rankings/top10")
    public ResponseEntity<List<RankingAwardCandidateResponse>> getTop10RankingByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID roundId
    ) {
        return ResponseEntity.ok(awardService.getTop10RankingByCategory(categoryId, roundId));
    }

    @Operation(
            summary = "Auto grant top 10 awards by pattern",
            description = "Read top 10 ranking in a category and publish awards based on the configured top 10 pattern.",
            operationId = "autoGrantTop10Awards"
    )
    @PostMapping("/categories/{categoryId}/auto-grant-top10")
    public ResponseEntity<List<AwardResponse>> autoGrantTop10Awards(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID roundId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(awardService.autoGrantTop10Awards(categoryId, roundId, principal.getUser().getUserId()));
    }
}
