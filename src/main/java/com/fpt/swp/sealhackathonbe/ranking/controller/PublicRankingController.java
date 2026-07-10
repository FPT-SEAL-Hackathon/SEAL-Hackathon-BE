package com.fpt.swp.sealhackathonbe.ranking.controller;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicRankingController {
    private final RankingService rankingService;

    @GetMapping("/api/v1/public/leaderboard/{eventId}/{categoryId}")
    @Operation(summary = "Get public leaderboard", description = "Retrieves the public leaderboard for a specific event and category")
    public ResponseEntity<List<EventRankingDTO>> getEventLeaderboardByCategory(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRankingDTO> rankings = rankingService.getPublishedCategoryLeaderboard(eventId,categoryId);
        return ResponseEntity.ok(rankings);
    }
}
