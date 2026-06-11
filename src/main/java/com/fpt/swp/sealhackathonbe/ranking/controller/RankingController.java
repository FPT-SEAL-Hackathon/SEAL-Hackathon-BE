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
    public ResponseEntity<List<EventRankingDTO>> computeEventRankings(
            @PathVariable("id") UUID eventId,
            @RequestParam UUID categoryId) {

        // MOCKED: Fetch teamIds for the event. In a real scenario, call EventService/SubmissionService.
        List<UUID> teamIds = new ArrayList<>();

        List<EventRankingDTO> rankings = rankingService.computeEventRankings(eventId, categoryId);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/admin/rounds/{roundId}/compute-rankings")
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

        List<EventRanking> rankings =

        // Sort by rank position
        rankings.sort((r1, r2) -> Integer.compare(r1.getRankPosition(), r2.getRankPosition()));

        List<EventRankingDTO> dtos = rankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
                .eventId(r.getEvent().getEventId())
                .categoryId(r.getCategory().getCategoryId())
                .teamId(r.getTeam().getTeamId())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
