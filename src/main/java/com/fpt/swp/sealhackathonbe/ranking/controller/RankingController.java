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

@RestController
@RequestMapping("/api/v1")
public class RankingController {

    private final RankingService rankingService;
    private final SubmissionQueryService submissionQueryService;

    @Autowired
    public RankingController(RankingService rankingService, SubmissionQueryService submissionQueryService) {
        this.rankingService = rankingService;
        this.submissionQueryService = submissionQueryService;
    }

    /**
     * Compute rankings for an event.
     */
    @PostMapping("/admin/events/{id}/compute-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<EventRankingDTO>> computeEventRankings(
            @PathVariable("id") UUID eventId,
            @RequestParam UUID categoryId) {

        // MOCKED: Fetch teamIds for the event. In a real scenario, call EventService/SubmissionService.
        List<UUID> teamIds = new ArrayList<>();

        List<EventRankingDTO> rankings = rankingService.computeEventRankings(eventId, categoryId);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/admin/rounds/{roundId}/compute-rankings")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<RoundRankingDTO>> computeRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId
    ){
        List<RoundRankingDTO> rankings = rankingService.computeRoundRankings(roundId,categoryId);
        return ResponseEntity.ok(rankings);
    }
    /**
     * Public leaderboard API.
     */
    @GetMapping("/public/leaderboard/{eventId}/{categoryId}")
    public ResponseEntity<List<EventRankingDTO>> getEventLeaderboardByCategory(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRankingDTO> rankings = rankingService.getCategoryLeaderboard(eventId,categoryId);

        // Sort by rank position
        rankings.sort((r1, r2) -> Integer.compare(r1.getRankPosition(), r2.getRankPosition()));
        return ResponseEntity.ok(rankings);
    }
}
