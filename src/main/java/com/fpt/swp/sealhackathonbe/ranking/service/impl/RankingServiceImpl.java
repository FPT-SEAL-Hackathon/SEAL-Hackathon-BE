package com.fpt.swp.sealhackathonbe.ranking.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.EventRankingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;


import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final JudgingService judgingService;
    private final RoundRankingRepository roundRankingRepository;
    private final EventRankingRepository eventRankingRepository;
    private final TeamDisqualificationService teamDisqualificationService;
    private final SubmissionDisqualificationService submissionDisqualificationService;
    private final SubmissionQueryService submissionQueryService;
    private final EntityManager entityManager;
    private final RoundService roundService;


    @Override
    @Transactional
    public List<RoundRankingDTO> computeRoundRankings(UUID roundId, UUID categoryId) {
        List<RoundRanking> rankings = new ArrayList<>();

        Round roundRef = entityManager.getReference(Round.class, roundId);
        Category categoryRef = entityManager.getReference(Category.class, categoryId);
        List<SubmissionResponse> submissionsList = submissionQueryService.getSubmissionsByRound(roundId);

        List<UUID> submissionIds = submissionsList.stream()
                .map(SubmissionResponse::getSubmissionId)
                .collect(Collectors.toList());

        List<UUID> disqualifiedSubIds = submissionDisqualificationService.getDisqualifiedSubmissions(roundId).stream()
                .map(DisqualifiedSubmissionResponse::getSubmissionId)
                .collect(Collectors.toList());

        List<UUID> teamIds = submissionQueryService.getSubmissionsByRound(roundId).stream()
                .map(SubmissionResponse::getTeamId)
                .collect(Collectors.toList());

        Set<UUID> disqualifiedTeamIds = teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId);

        Map<UUID, UUID> submissionToTeamMap = submissionsList.stream().collect(Collectors.toMap(
                SubmissionResponse::getSubmissionId,
                SubmissionResponse::getTeamId
        ));

        // 1. Lấy toàn bộ điểm của tất cả submissions, nhóm lại thành Map<SubmissionID, List<Judging>>
        Map<UUID, List<Judging>> judgingsMap = judgingService.getJudgingsGroupedBySubmissionIds(submissionIds);


        for (UUID submissionId : submissionIds) {
            UUID teamId = submissionToTeamMap.get(submissionId);
            if (teamId == null) continue;

            Teams teamRef = entityManager.getReference(Teams.class, teamId);
            Submissions submissionRef = entityManager.getReference(Submissions.class, submissionId);

            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal averageScore = BigDecimal.ZERO;

            // KIỂM TRA VI PHẠM (Dùng hàm .contains() của Set cực nhanh)
            boolean isDisqualified = disqualifiedSubIds.contains(submissionId) || disqualifiedTeamIds.contains(teamId);
            if (!isDisqualified) {
                // Lấy danh sách điểm từ Map thay vì gọi DB
                List<Judging> judgings = judgingsMap.getOrDefault(submissionId, Collections.emptyList());

                if (!judgings.isEmpty()) {
                    for (Judging j : judgings) {
                        if (j.getScoreValue() != null) {
                            totalScore = totalScore.add(j.getScoreValue());
                        }
                    }
                    averageScore = totalScore.divide(BigDecimal.valueOf(judgings.size()), 4, RoundingMode.HALF_UP);
                }
            }

            RoundRanking ranking = new RoundRanking();
            ranking.setRound(roundRef);
            ranking.setCategory(categoryRef);
            ranking.setTeam(teamRef);
            ranking.setSubmission(submissionRef);
            ranking.setTotalScore(totalScore);
            ranking.setAverageScore(averageScore);
            ranking.setRankPosition(0);
            ranking.setIsAdvanced(false);

            rankings.add(ranking);
        }

        // Sort by total score descending
        rankings.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));

        int currentRank = 1;
        int advancementN = roundService.getAdvancementTopN();
        for (int i = 0; i < rankings.size(); i++) {
            if (i > 0 && rankings.get(i).getTotalScore().compareTo(rankings.get(i - 1).getTotalScore()) < 0) {
                currentRank = i + 1;
            }
            rankings.get(i).setRankPosition(currentRank);
            if(currentRank <= advancementN){
                rankings.get(i).setIsAdvanced(true);
            }
        }

        // Save to DB
        // Clear previous rankings for this round and category if needed, or update.
        // For simplicity, we just save them (assuming caller manages cleanup or relies on unique constraint update)
        List<RoundRanking> savedRankings = roundRankingRepository.saveAll(rankings);

        return savedRankings.stream().map(r -> RoundRankingDTO.builder()
                .id(r.getId())
                .roundId(roundId)
                .categoryId(categoryId)
                .teamId(r.getTeam().getTeamId()) // May throw lazy initialization exception if ID is not accessible directly, but Hibernate proxy usually handles getId().
                .submissionId(r.getSubmission().getSubmissionId())
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
    public List<EventRankingDTO> computeEventRankings(UUID eventId, UUID categoryId) {
        List<EventRanking> rankings = new ArrayList<>();

        Event eventRef = entityManager.getReference(Event.class, eventId);
        Category categoryRef = entityManager.getReference(Category.class, categoryId);
        Round finalRound = roundService.getFinalRound(categoryId);

        List<UUID> teamIds = submissionQueryService.getSubmissionsByRound(finalRound.getRoundId()).stream()
                .map(SubmissionResponse::getTeamId)
                .collect(Collectors.toList());

        Set<UUID> disqualifiedTeamIds = TeamDisqualificationService.getDisqualifiedTeam;

        Map<UUID, BigDecimal> teamFinalRoundScores = new HashMap<>();

        if (finalRound != null) {
            List<RoundRanking> finalRoundRankings = roundRankingRepository.findByRoundRoundIdAndTeamTeamIdIn(finalRound.getRoundId(), teamIds);
            teamFinalRoundScores = finalRoundRankings.stream()
                    .collect(Collectors.toMap(
                            r -> r.getTeam().getTeamId(),
                            RoundRanking::getTotalScore
                    ));
        }

        for (UUID teamId : teamIds) {
            Teams teamRef = entityManager.getReference(Teams.class, teamId);
            BigDecimal finalScore = BigDecimal.ZERO;

            // Nếu Đội không bị tước tư cách -> Lấy điểm từ Vòng Chung Kết
            if (!disqualifiedTeamIds.contains(teamId)) {
                finalScore = teamFinalRoundScores.getOrDefault(teamId, BigDecimal.ZERO);
            }

            EventRanking ranking = new EventRanking();
            ranking.setEvent(eventRef);
            ranking.setCategory(categoryRef); // Ranking phân chia chuẩn theo Category
            ranking.setTeam(teamRef);
            ranking.setFinalScore(finalScore);
            ranking.setRankPosition(0);

            rankings.add(ranking);
        }

        // Sort and Rank
        rankings.sort((r1, r2) -> r2.getFinalScore().compareTo(r1.getFinalScore()));

        int currentRank = 1;
        for (int i = 0; i < rankings.size(); i++) {
            // Nếu điểm đội hiện tại nhỏ hơn đội đứng trước -> Rớt hạng
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
                .teamId(r.getTeam().getTeamId())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRankingDTO> getCategoryLeaderboard(UUID eventId, UUID categoryId) {
        List<EventRanking> rankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
        return rankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
                .eventId(eventId)
                .categoryId(categoryId)
                .teamId(r.getTeam().getTeamId())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .build()
        ).collect(Collectors.toList());
    }


}
