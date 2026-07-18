package com.fpt.swp.sealhackathonbe.ranking.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.RankingStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;

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
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;


import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingServiceImpl implements RankingService {

    private final JudgingService judgingService;
    private final RoundRankingRepository roundRankingRepository;
    private final EventRankingRepository eventRankingRepository;
    private final TeamDisqualificationService teamDisqualificationService;
    private final SubmissionDisqualificationService submissionDisqualificationService;
    private final SubmissionQueryService submissionQueryService;
    private final EntityManager entityManager;
    private final RoundService roundService;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final TeamMembersRepository teamMembersRepository;


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
                .toList();


        List<UUID> disqualifiedTeamIds = teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)
                .stream()
                .map(DisqualifiedTeamResponse:: getTeamId)
                .toList();

        Map<UUID, UUID> submissionToTeamMap = submissionsList.stream().collect(Collectors.toMap(
                SubmissionResponse::getSubmissionId,
                SubmissionResponse::getTeamId
        ));

        // Lấy danh sách ranking hiện có để update thay vì insert mới (tránh lỗi UNIQUE KEY)
        List<RoundRanking> existingRankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        Map<UUID, RoundRanking> existingRankingMap = existingRankings.stream()
                .collect(Collectors.toMap(r -> r.getTeam().getTeamId(), r -> r));

        // 1. Lấy toàn bộ điểm của tất cả submissions, nhóm lại thành Map<SubmissionID, List<Judging>>
        Map<UUID, List<Judging>> judgingsMap = judgingService.getJudgingsGroupedBySubmissionIds(submissionIds);


        for (UUID submissionId : submissionIds) {
            UUID teamId = submissionToTeamMap.get(submissionId);
            if (teamId == null) continue;

            Teams teamRef = entityManager.getReference(Teams.class, teamId);
            Submissions submissionRef = entityManager.getReference(Submissions.class, submissionId);

            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal averageScore = BigDecimal.ZERO;

            // KIỂM TRA VI PHẠM (Chỉ kiểm tra submission bị disqualify, team disqualify do rớt hạng vẫn giữ điểm)
            boolean isDisqualified = disqualifiedSubIds.contains(submissionId);
            if (!isDisqualified) {
                // Lấy danh sách điểm từ Map thay vì gọi DB
                List<Judging> judgings = judgingsMap.getOrDefault(submissionId, Collections.emptyList());

                if (!judgings.isEmpty()) {
                    int validScoreCount = 0;
                    for (Judging j : judgings) {
                        // CHỈ CỘNG ĐIỂM THẬT: Bỏ qua các điểm được đánh dấu là chấm hiệu chuẩn (Calibration)
                        if (j.getScoreValue() != null && !Boolean.TRUE.equals(j.getIsCalibration())) {
                            totalScore = totalScore.add(j.getScoreValue().multiply(j.getRoundCriterion().getWeight()));
                            validScoreCount++;
                        }
                    }
                    if (validScoreCount > 0) {
                        averageScore = totalScore.divide(BigDecimal.valueOf(validScoreCount), 4, RoundingMode.HALF_UP);
                    }
                }
            }

            // Cập nhật record cũ nếu đã tồn tại, hoặc tạo mới nếu chưa
            RoundRanking ranking = existingRankingMap.getOrDefault(teamId, new RoundRanking());
            ranking.setRound(roundRef);
            ranking.setCategory(categoryRef);
            ranking.setTeam(teamRef);
            ranking.setSubmission(submissionRef);
            ranking.setTotalScore(totalScore);
            ranking.setAverageScore(averageScore);
            ranking.setRankPosition(0);
            ranking.setIsPublished(false);
            ranking.setIsAdvanced(false);

            rankings.add(ranking);
        }

        // Sort by total score descending
        rankings.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));

        int currentRank = 1;
        Integer topN = roundService.getAdvancementTopN(roundId);
        int advancementN = topN != null ? topN : 0;
        
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
                .categoryName(r.getCategory().getCategoryName())
                .teamId(r.getTeam().getTeamId())
                .teamName(r.getTeam().getTeamName())
                .submissionId(r.getSubmission().getSubmissionId())
                .totalScore(r.getTotalScore())
                .averageScore(r.getAverageScore())
                .rankPosition(r.getRankPosition())
                .isAdvanced(r.getIsAdvanced())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void publishRoundRankings(UUID roundId, UUID categoryId, UUID adminUserId) {
        List<RoundRanking> existingRankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Rankings must be computed before publishing.");
        }
        List<UUID> recipientIds = new ArrayList<>();
        for (RoundRanking r : existingRankings) {
            r.setIsPublished(true);
            
            List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(r.getTeam().getTeamId());
            members.forEach(m -> recipientIds.add(m.getUserId()));

            // Disqualify team if they did not advance and are not already disqualified
            if (Boolean.FALSE.equals(r.getIsAdvanced()) && !TeamStatusConstants.DISQUALIFIED.equals(r.getTeam().getTeamStatusId())) {
                try {
                    DisqualifyTeamRequest req = new DisqualifyTeamRequest();
                    req.setReason("Did not reach the top positions to advance to the next round");
                    teamDisqualificationService.disqualifyTeam(r.getTeam().getTeamId(), req, adminUserId);
                } catch (Exception e) {
                    log.warn("Failed to automatically disqualify team {} (probably already disqualified): {}", 
                             r.getTeam().getTeamId(), e.getMessage());
                }
            }
        }
        roundRankingRepository.saveAll(existingRankings);

        if (!recipientIds.isEmpty()) {
            String roundName = existingRankings.get(0).getRound().getRoundName();
            UUID eventId = existingRankings.get(0).getRound().getCategory().getEvent().getEventId();
            
            notificationService.sendBroadcastNotification(
                    recipientIds.stream().distinct().toList(),
                    adminUserId,
                    eventId,
                    "Round Leaderboard Published",
                    "The leaderboard for round " + roundName + " has been published. Go to the Leaderboard page to see the results!"
            );
        }
    }

    @Override
    @Transactional
    public List<EventRankingDTO> computeEventRankings(UUID eventId) {
        Event eventRef = entityManager.find(Event.class, eventId);
        if (eventRef == null) throw new IllegalArgumentException("Event ID does not exist: " + eventId);
        
        List<Category> categories = categoryRepository.findByEventEventId(eventId);
        List<EventRankingDTO> allComputedRankings = new ArrayList<>();

        for (Category categoryRef : categories) {
            UUID categoryId = categoryRef.getCategoryId();
            List<EventRanking> rankings = new ArrayList<>();

            RoundResponse finalRound = null;
            try {
                finalRound = roundService.getFinalRound(categoryId);
            } catch (RuntimeException e) {
                log.warn("Skipping Event Ranking computation for Category {} because it has no rounds.", categoryRef.getCategoryName());
                continue;
            }

            if (finalRound == null) {
                log.warn("Skipping Event Ranking computation for Category {} because final round is null.", categoryRef.getCategoryName());
                continue;
            }

            // (Removed strict "Completed" check to allow computation if rankings exist)

            List<UUID> teamIds = entityManager.createQuery(
                    "SELECT t.teamId FROM Teams t WHERE t.category.categoryId = :categoryId AND t.event.eventId = :eventId", UUID.class)
                    .setParameter("categoryId", categoryId)
                    .setParameter("eventId", eventId)
                    .getResultList();

            List<UUID> disqualifiedTeamIds = teamDisqualificationService.getDisqualifiedTeams(finalRound.getRoundId(), categoryId)
                    .stream()
                    .map(DisqualifiedTeamResponse:: getTeamId)
                    .toList();
            
            // Lấy danh sách ranking hiện có của Event & Category để update thay vì insert mới
            // Get existing Event & Category rankings to update instead of inserting new ones
            List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
            Map<UUID, EventRanking> existingRankingMap = existingRankings.stream()
                    .collect(Collectors.toMap(r -> r.getTeam().getTeamId(), r -> r));

            List<RoundRanking> finalRoundRankings = roundRankingRepository.findByRoundRoundIdAndTeamTeamIdIn(finalRound.getRoundId(), teamIds);
            
            if (finalRoundRankings.isEmpty() && !teamIds.isEmpty()) {
                log.warn("Skipping Event Ranking computation for Category {} because final round rankings have not been computed.", categoryRef.getCategoryName());
                continue;
            }

            Map<UUID, BigDecimal> dScores = finalRoundRankings.stream()
                    .collect(Collectors.toMap(
                            r -> r.getTeam().getTeamId(),
                            RoundRanking::getTotalScore
                    ));

            for (UUID teamId : teamIds) {
                Teams teamRef = entityManager.getReference(Teams.class, teamId);
                BigDecimal finalScore = BigDecimal.ZERO;

                // Nếu Đội không bị tước tư cách -> Lấy điểm từ Vòng Chung Kết
                if (!disqualifiedTeamIds.contains(teamId)) {
                    finalScore = dScores.getOrDefault(teamId, BigDecimal.ZERO);
                }

                // Cập nhật record cũ nếu đã tồn tại, hoặc tạo mới nếu chưa
                EventRanking ranking = existingRankingMap.getOrDefault(teamId, new EventRanking());
                ranking.setEvent(eventRef);
                ranking.setCategory(categoryRef); // Ranking phân chia chuẩn theo Category
                ranking.setTeam(teamRef);
                ranking.setFinalScore(finalScore);
                ranking.setRankPosition(0);
                ranking.setIsPublished(false);

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

            allComputedRankings.addAll(savedRankings.stream().map(r -> EventRankingDTO.builder()
                    .id(r.getId())
                    .eventId(eventId)
                    .categoryId(categoryId)
                    .categoryName(r.getCategory().getCategoryName())
                    .teamId(r.getTeam().getTeamId())
                    .teamName(r.getTeam().getTeamName())
                    .finalScore(r.getFinalScore())
                    .rankPosition(r.getRankPosition())
                    .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                    .isPublished(r.getIsPublished())
                    .build()
            ).collect(Collectors.toList()));
        }

        return allComputedRankings;
    }

    @Override
    @Transactional
    public void publishEventRankings(UUID eventId, UUID categoryId) {
        List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Event rankings must be computed before publishing.");
        }
        for (EventRanking r : existingRankings) {
            r.setIsPublished(true);
        }
        eventRankingRepository.saveAll(existingRankings);
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
                .isPublished(r.getIsPublished())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRankingDTO> getPublishedCategoryLeaderboard(UUID eventId, UUID categoryId) {
        List<EventRankingDTO> rankings = getCategoryLeaderboard(eventId, categoryId);
        if (rankings.isEmpty()) {
            return rankings;
        }
        boolean isPublished = rankings.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsPublished()));
        if (!isPublished) {
            throw new IllegalStateException("Leaderboard has not been published yet.");
        }
        return rankings.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPublished()))
                .sorted((r1, r2) -> Integer.compare(r1.getRankPosition(), r2.getRankPosition()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundRankingDTO> getPublishedRoundLeaderboard(UUID roundId, UUID categoryId) {
        List<RoundRankingDTO> rankings = getRoundRankings(roundId, categoryId);
        if (rankings.isEmpty()) {
            return rankings;
        }
        boolean isPublished = rankings.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsPublished()));
        if (!isPublished) {
            throw new IllegalStateException("Round Leaderboard has not been published yet.");
        }
        return rankings.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPublished()))
                .sorted(Comparator.comparing(RoundRankingDTO::getRankPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundRankingDTO> getRoundRankings(UUID roundId, UUID categoryId) {
        List<RoundRanking> rankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        return rankings.stream().map(r -> RoundRankingDTO.builder()
                .id(r.getId())
                .roundId(roundId)
                .categoryId(categoryId)
                .categoryName(r.getCategory().getCategoryName())
                .teamId(r.getTeam().getTeamId())
                .teamName(r.getTeam().getTeamName())
                .submissionId(r.getSubmission().getSubmissionId())
                .totalScore(r.getTotalScore())
                .averageScore(r.getAverageScore())
                .rankPosition(r.getRankPosition())
                .isAdvanced(r.getIsAdvanced())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .build()
        ).sorted(Comparator.comparingInt(RoundRankingDTO::getRankPosition)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRankingDTO> getAdminEventRankings(UUID eventId) {
        List<EventRanking> rankings = eventRankingRepository.findByEvent_EventId(eventId);
        return rankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
                .eventId(eventId)
                .categoryId(r.getCategory().getCategoryId())
                .categoryName(r.getCategory().getCategoryName())
                .teamId(r.getTeam().getTeamId())
                .teamName(r.getTeam().getTeamName())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .build()
        ).sorted(Comparator.comparingInt(EventRankingDTO::getRankPosition)).collect(Collectors.toList());
    }
}
