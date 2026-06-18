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
            summary = "Create or update award patterns",
            description = "Configure award title, tier, description, and prize by rank in a category.",
            operationId = "saveAwardPatterns"
    )
    @PostMapping("/categories/{categoryId}/award-patterns")
    public ResponseEntity<List<AwardPatternResponse>> saveAwardPatterns(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AwardPatternRequest request
    ) {
        return ResponseEntity.ok(awardService.saveAwardPatterns(categoryId, request));
    }

    @Operation(
            summary = "Get award patterns",
            description = "Get active award patterns in a category.",
            operationId = "getAwardPatterns"
    )
    @GetMapping("/categories/{categoryId}/award-patterns")
    public ResponseEntity<List<AwardPatternResponse>> getAwardPatterns(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(awardService.getAwardPatterns(categoryId));
    }

    @Operation(
            summary = "Get top ranking by category",
            description = "Get top ranked teams from RoundRankings by category. If roundId is omitted, the latest round of the category is used.",
            operationId = "getTopRankingByCategory"
    )
    @GetMapping("/categories/{categoryId}/rankings/top")
    public ResponseEntity<List<RankingAwardCandidateResponse>> getTopRankingByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(awardService.getTopRankingByCategory(categoryId, roundId, limit));
    }

    @Operation(
            summary = "Auto grant top awards by pattern",
            description = "Read top ranking in a category and publish awards based on configured award patterns.",
            operationId = "autoGrantTopAwards"
    )
    @PostMapping("/categories/{categoryId}/auto-grant-top")
    public ResponseEntity<List<AwardResponse>> autoGrantTopAwards(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(awardService.autoGrantTopAwards(categoryId, roundId, principal.getUser().getUserId(), limit));
    }
}
