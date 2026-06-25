package com.fpt.swp.sealhackathonbe.ranking.controller;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ranking Controller", description = "APIs for computing rankings and fetching leaderboards")
public class RankingController {

    private final RankingService rankingService;

    @Autowired
    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Compute rankings for an event.
     */
    @PostMapping("/admin/events/{id}/compute-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Compute rankings for an event", description = "Calculates the final rankings for all submissions in an event")
    public ResponseEntity<List<EventRankingDTO>> computeEventRankings(
            @PathVariable("id") UUID eventId) {

        List<EventRankingDTO> rankings = rankingService.computeEventRankings(eventId);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/admin/rounds/{roundId}/compute-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Compute rankings for a round", description = "Calculates the rankings for submissions in a specific round and category")
    public ResponseEntity<List<RoundRankingDTO>> computeRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId
    ){
        List<RoundRankingDTO> rankings = rankingService.computeRoundRankings(roundId,categoryId);
        return ResponseEntity.ok(rankings);
    }
    @PostMapping("/admin/rounds/{roundId}/publish-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Publish rankings for a round", description = "Publishes the computed rankings for a specific round and category")
    public ResponseEntity<Void> publishRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId
    ){
        rankingService.publishRoundRankings(roundId, categoryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/events/{eventId}/publish-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Publish rankings for an event", description = "Publishes the computed final rankings for a specific event and category")
    public ResponseEntity<Void> publishEventRankings(
            @PathVariable("eventId") UUID eventId,
            @RequestParam UUID categoryId
    ){
        rankingService.publishEventRankings(eventId, categoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * Public leaderboard API.
     */
    @GetMapping("/public/leaderboard/{eventId}/{categoryId}")
    @Operation(summary = "Get public leaderboard", description = "Retrieves the public leaderboard for a specific event and category")
    public ResponseEntity<List<EventRankingDTO>> getEventLeaderboardByCategory(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRankingDTO> rankings = rankingService.getCategoryLeaderboard(eventId,categoryId);

        // Sort by rank position and filter only published
        rankings = rankings.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPublished()))
                .sorted((r1, r2) -> Integer.compare(r1.getRankPosition(), r2.getRankPosition()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(rankings);
    }
}
