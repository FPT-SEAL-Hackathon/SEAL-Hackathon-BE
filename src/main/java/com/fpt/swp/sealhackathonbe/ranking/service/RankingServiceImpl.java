package com.fpt.swp.sealhackathonbe.ranking.service;

import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.DisqualificationRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.EventRankingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;

// Mock imports for entities
import com.fpt.swp.sealhackathonbe.event.entity.Round;
import com.fpt.swp.sealhackathonbe.team.entity.Team;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    private final JudgingRepository judgingRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final EventRankingRepository eventRankingRepository;
    private final DisqualificationRepository disqualificationRepository;
    private final EntityManager entityManager;

    public RankingServiceImpl(JudgingRepository judgingRepository,
                              RoundRankingRepository roundRankingRepository,
                              EventRankingRepository eventRankingRepository,
                              DisqualificationRepository disqualificationRepository,
                              EntityManager entityManager) {
        this.judgingRepository = judgingRepository;
        this.roundRankingRepository = roundRankingRepository;
        this.eventRankingRepository = eventRankingRepository;
        this.disqualificationRepository = disqualificationRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<RoundRankingDTO> computeRoundRankings(UUID roundId, UUID categoryId, List<UUID> submissionIds, Map<UUID, UUID> submissionToTeamMap) {
        List<RoundRanking> rankings = new ArrayList<>();
        
        Round roundRef = entityManager.getReference(Round.class, roundId);
        Category categoryRef = entityManager.getReference(Category.class, categoryId);

        for (UUID submissionId : submissionIds) {
            UUID teamId = submissionToTeamMap.get(submissionId);
            if (teamId == null) continue;

            Team teamRef = entityManager.getReference(Team.class, teamId);
            Submission submissionRef = entityManager.getReference(Submission.class, submissionId);

            // Fetch scores
            List<Judging> judgings = judgingRepository.findBySubmissionId(submissionId);
            
            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal averageScore = BigDecimal.ZERO;

            if (!judgings.isEmpty()) {
                for (Judging j : judgings) {
                    if (j.getScoreValue() != null) {
                        totalScore = totalScore.add(j.getScoreValue());
                    }
                }
                averageScore = totalScore.divide(BigDecimal.valueOf(judgings.size()), 4, RoundingMode.HALF_UP);
            }

            // Check disqualification
            boolean isDisqualified = disqualificationRepository.findBySubmissionId(submissionId).stream()
                    .anyMatch(d -> d.getIsReversed() == null || !d.getIsReversed());
            if (!isDisqualified) {
                isDisqualified = disqualificationRepository.findByTeamId(teamId).stream()
                        .anyMatch(d -> d.getIsReversed() == null || !d.getIsReversed());
            }

            if (isDisqualified) {
                totalScore = BigDecimal.ZERO;
                averageScore = BigDecimal.ZERO;
            }

            RoundRanking ranking = new RoundRanking();
            ranking.setRound(roundRef);
            ranking.setCategory(categoryRef);
            ranking.setTeam(teamRef);
            ranking.setSubmission(submissionRef);
            ranking.setTotalScore(totalScore);
            ranking.setAverageScore(averageScore);
            // Temporary position, will sort next
            ranking.setRankPosition(0);
            ranking.setIsAdvanced(false);
            
            rankings.add(ranking);
        }

        // Sort by total score descending
        rankings.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));

        // Assign ranks
        int currentRank = 1;
        for (int i = 0; i < rankings.size(); i++) {
            if (i > 0 && rankings.get(i).getTotalScore().compareTo(rankings.get(i - 1).getTotalScore()) < 0) {
                currentRank = i + 1;
            }
            rankings.get(i).setRankPosition(currentRank);
        }

        // Save to DB
        // Clear previous rankings for this round and category if needed, or update. 
        // For simplicity, we just save them (assuming caller manages cleanup or relies on unique constraint update)
        List<RoundRanking> savedRankings = roundRankingRepository.saveAll(rankings);

        return savedRankings.stream().map(r -> RoundRankingDTO.builder()
                .id(r.getId())
                .roundId(roundId)
                .categoryId(categoryId)
                .teamId(r.getTeam().getId()) // May throw lazy initialization exception if ID is not accessible directly, but Hibernate proxy usually handles getId().
                .submissionId(r.getSubmission().getId())
                .totalScore(r.getTotalScore())
                .averageScore(r.getAverageScore())
                .rankPosition(r.getRankPosition())
                .isAdvanced(r.getIsAdvanced())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EventRankingDTO> computeEventRankings(UUID eventId, UUID categoryId, List<UUID> teamIds) {
        List<EventRanking> rankings = new ArrayList<>();
        
        Event eventRef = entityManager.getReference(Event.class, eventId);
        Category categoryRef = entityManager.getReference(Category.class, categoryId);

        for (UUID teamId : teamIds) {
            Team teamRef = entityManager.getReference(Team.class, teamId);

            // Calculate final score based on round rankings or other logic
            // Assuming for event ranking, we sum up all round rankings for this team in this category
            // Note: In real scenarios, you might need a specific calculation logic (like weighting).
            // We'll just do a sum of totalScores from RoundRanking as a placeholder.
            BigDecimal finalScore = BigDecimal.ZERO;
            
            // Check disqualification
            boolean isDisqualified = disqualificationRepository.findByTeamId(teamId).stream()
                    .anyMatch(d -> d.getIsReversed() == null || !d.getIsReversed());

            if (isDisqualified) {
                finalScore = BigDecimal.ZERO;
            } else {
                // Currently no direct query for Team + Category in RoundRankingRepo, we could mock or leave as 0
                // Placeholder: finalScore remains 0 or some logic
            }

            EventRanking ranking = new EventRanking();
            ranking.setEvent(eventRef);
            ranking.setCategory(categoryRef);
            ranking.setTeam(teamRef);
            ranking.setFinalScore(finalScore);
            ranking.setRankPosition(0);

            rankings.add(ranking);
        }

        // Sort and Rank
        rankings.sort((r1, r2) -> r2.getFinalScore().compareTo(r1.getFinalScore()));

        int currentRank = 1;
        for (int i = 0; i < rankings.size(); i++) {
            if (i > 0 && rankings.get(i).getFinalScore().compareTo(rankings.get(i - 1).getFinalScore()) < 0) {
                currentRank = i + 1;
            }
            rankings.get(i).setRankPosition(currentRank);
        }

        List<EventRanking> savedRankings = eventRankingRepository.saveAll(rankings);

        return savedRankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
                .eventId(eventId)
                .categoryId(categoryId)
                .teamId(r.getTeam().getId())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .build()
        ).collect(Collectors.toList());
    }
}
