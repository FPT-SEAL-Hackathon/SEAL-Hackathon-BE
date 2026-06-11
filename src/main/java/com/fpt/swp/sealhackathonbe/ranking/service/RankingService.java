package com.fpt.swp.sealhackathonbe.ranking.service;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RankingService {
    /**
     * Compute rankings for a specific round and category based on judge scores.
     * @param roundId The ID of the round.
     * @param categoryId The ID of the category.
     * @return List of computed round rankings.
     */
    List<RoundRankingDTO> computeRoundRankings(UUID roundId, UUID categoryId);

    /**
     * Compute final event rankings.
     * @param eventId The ID of the event.
     * @param categoryId The ID of the category.
     * @return List of computed event rankings.
     */
    List<EventRankingDTO> computeEventRankings(UUID eventId, UUID categoryId);

    List<EventRankingDTO> getCategoryLeaderboard(UUID eventId, UUID categoryId);
}
