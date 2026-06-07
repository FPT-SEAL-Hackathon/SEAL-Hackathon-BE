package com.fpt.swp.sealhackathonbe.ranking.controller;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.EventRankingRepository;
import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
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
    private final EventRankingRepository eventRankingRepository;

    @Autowired
    public RankingController(RankingService rankingService, EventRankingRepository eventRankingRepository) {
        this.rankingService = rankingService;
        this.eventRankingRepository = eventRankingRepository;
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
        
        List<EventRankingDTO> rankings = rankingService.computeEventRankings(eventId, categoryId, teamIds);
        return ResponseEntity.ok(rankings);
    }

    /**
     * Public leaderboard API.
     */
    @GetMapping("/public/leaderboard/{eventId}/{categoryId}")
    public ResponseEntity<List<EventRankingDTO>> getPublicLeaderboard(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRanking> rankings = eventRankingRepository.findByEventIdAndCategoryId(eventId, categoryId);
        
        // Sort by rank position
        rankings.sort((r1, r2) -> Integer.compare(r1.getRankPosition(), r2.getRankPosition()));

        List<EventRankingDTO> dtos = rankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
//                .eventId(r.getEvent().getId())
//                .categoryId(r.getCategory().getId())
//                .teamId(r.getTeam().getId())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
