package com.fpt.swp.sealhackathonbe.ranking.service;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;

import java.util.List;
import java.util.UUID;

public interface RankingService {
    /**
     * Compute rankings for a specific round and category based on judge scores.
     * @param roundId The ID of the round.
     * @param categoryId The ID of the category.
     * @return List of computed round rankings.
     */
    List<RoundRankingDTO> computeRoundRankings(UUID roundId, UUID categoryId);
    void publishRoundRankings(UUID roundId, UUID categoryId, UUID adminUserId);
    void approveRoundRankings(UUID roundId, UUID categoryId, UUID adminUserId);

    /**
     * Compute final event rankings.
     * @param eventId The ID of the event.
     * @return List of computed event rankings.
     */
    List<EventRankingDTO> computeEventRankings(UUID eventId);
    void publishEventRankings(UUID eventId, UUID categoryId);

    List<EventRankingDTO> computeCategoryEventRankings(UUID categoryId);
    void publishCategoryEventRankings(UUID categoryId, UUID adminUserId);
    void approveCategoryEventRankings(UUID categoryId, UUID adminUserId);

    List<EventRankingDTO> getCategoryLeaderboard(UUID eventId, UUID categoryId);
    List<EventRankingDTO> getPublishedCategoryLeaderboard(UUID eventId, UUID categoryId);

    List<RoundRankingDTO> getRoundRankings(UUID roundId, UUID categoryId);
    List<RoundRankingDTO> getPublishedRoundLeaderboard(UUID roundId, UUID categoryId);
    List<EventRankingDTO> getAdminEventRankings(UUID eventId);
}
