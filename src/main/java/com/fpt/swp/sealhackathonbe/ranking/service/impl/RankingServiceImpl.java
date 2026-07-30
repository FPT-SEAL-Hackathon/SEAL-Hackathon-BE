package com.fpt.swp.sealhackathonbe.ranking.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.RankingStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;

import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import com.fpt.swp.sealhackathonbe.appeal.repository.AppealRepository;
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
@Transactional(readOnly = true)
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
    private final AppealRepository appealRepository;


    @Override
    @Transactional
    public List<RoundRankingDTO> computeRoundRankings(UUID roundId, UUID categoryId) {
        List<RoundRanking> rankings = new ArrayList<>();

        Round roundRef = entityManager.find(Round.class, roundId);
        if (roundRef == null) throw new IllegalArgumentException("Round ID does not exist: " + roundId);
        if (Boolean.TRUE.equals(roundRef.getIsCalibrationRound())) {
            throw new IllegalStateException("Cannot compute rankings for a calibration round.");
        }
        Category categoryRef = entityManager.find(Category.class, categoryId);
        if (categoryRef == null) throw new IllegalArgumentException("Category ID does not exist: " + categoryId);
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

        // KIỂM TRA ĐIỂM ĐÃ ĐƯỢC DUYỆT (FINALIZED)
        validateScoresFinalizedForRound(submissionsList, disqualifiedSubIds, "compute");

        Map<UUID, UUID> submissionToTeamMap = submissionsList.stream().collect(Collectors.toMap(
                SubmissionResponse::getSubmissionId,
                SubmissionResponse::getTeamId
        ));

        Map<UUID, String> teamNameMap = submissionsList.stream().collect(Collectors.toMap(
                SubmissionResponse::getTeamId,
                SubmissionResponse::getTeamName
        ));

        // Lấy danh sách ranking hiện có để update thay vì insert mới (tránh lỗi UNIQUE KEY)
        List<RoundRanking> existingRankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        if (existingRankings.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsApproved()))) {
            throw new IllegalStateException("Cannot compute rankings because they have been approved and locked.");
        }
        
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
                    Set<UUID> judgeIds = new HashSet<>();
                    for (Judging j : judgings) {
                        // CHỈ CỘNG ĐIỂM THẬT: Bỏ qua các điểm được đánh dấu là chấm hiệu chuẩn (Calibration)
                        if (j.getScoreValue() != null && !Boolean.TRUE.equals(j.getIsCalibration())) {
                            totalScore = totalScore.add(j.getScoreValue().multiply(j.getRoundCriterion().getWeight()));
                            judgeIds.add(j.getRoundJudge().getRoundJudgeId());
                        }
                    }
                    if (!judgeIds.isEmpty()) {
                        averageScore = totalScore.divide(BigDecimal.valueOf(judgeIds.size()), 4, RoundingMode.HALF_UP);
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
            ranking.setIsApproved(false);
            ranking.setIsAdvanced(false);

            rankings.add(ranking);
        }

        // Sort by average score descending, tie-breaker: submission time ascending
        rankings.sort((r1, r2) -> {
            BigDecimal s1 = r1.getAverageScore() != null ? r1.getAverageScore() : BigDecimal.ZERO;
            BigDecimal s2 = r2.getAverageScore() != null ? r2.getAverageScore() : BigDecimal.ZERO;
            int scoreCompare = s2.compareTo(s1);
            if (scoreCompare != 0) return scoreCompare;
            LocalDateTime t1 = r1.getSubmission().getLastUpdatedAt();
            LocalDateTime t2 = r2.getSubmission().getLastUpdatedAt();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        });

        int currentRank = 1;
        Integer topN = roundService.getAdvancementTopN(roundId);
        int advancementN = topN != null ? topN : 0;
        int validRankCount = 0;
        RoundRanking prevValid = null;

        for (int i = 0; i < rankings.size(); i++) {
            RoundRanking current = rankings.get(i);
            boolean isDisqualified = disqualifiedSubIds.contains(current.getSubmission().getSubmissionId()) 
                                  || disqualifiedTeamIds.contains(current.getTeam().getTeamId());
            boolean hasZeroScore = current.getAverageScore() == null || current.getAverageScore().compareTo(BigDecimal.ZERO) == 0;

            if (isDisqualified || hasZeroScore) {
                current.setRankPosition(0);
                current.setIsAdvanced(false);
                continue;
            }

            if (validRankCount > 0 && prevValid != null) {
                BigDecimal cScore = current.getAverageScore() != null ? current.getAverageScore() : BigDecimal.ZERO;
                BigDecimal pScore = prevValid.getAverageScore() != null ? prevValid.getAverageScore() : BigDecimal.ZERO;
                int scoreCompare = cScore.compareTo(pScore);
                if (scoreCompare < 0) {
                    currentRank = validRankCount + 1;
                } else if (scoreCompare == 0) {
                    LocalDateTime t1 = prevValid.getSubmission().getLastUpdatedAt();
                    LocalDateTime t2 = current.getSubmission().getLastUpdatedAt();
                    if (t1 != null && t2 != null && t2.isAfter(t1)) {
                        currentRank = validRankCount + 1;
                    }
                }
            }
            current.setRankPosition(currentRank);
            if (currentRank <= advancementN) {
                current.setIsAdvanced(true);
            } else {
                current.setIsAdvanced(false);
            }
            validRankCount++;
            prevValid = current;
        }

        // Save to DB
        List<RoundRanking> savedRankings = roundRankingRepository.saveAll(rankings);

        String categoryName = categoryRef.getCategoryName();

        return savedRankings.stream().map(r -> RoundRankingDTO.builder()
                .id(r.getId())
                .roundId(roundId)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .teamId(r.getTeam().getTeamId())
                .teamName(teamNameMap.get(r.getTeam().getTeamId()))
                .submissionId(r.getSubmission().getSubmissionId())
                .totalScore(r.getTotalScore())
                .averageScore(r.getAverageScore())
                .rankPosition(r.getRankPosition())
                .isAdvanced(r.getIsAdvanced())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .isApproved(r.getIsApproved())
                .build()
        ).sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void publishRoundRankings(UUID roundId, UUID categoryId, UUID adminUserId, Integer appealDurationMinutes) {
        List<RoundRanking> existingRankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Rankings must be computed before publishing.");
        }
        
        validateScoresFinalizedForRound(roundId, "publish");
        
        // Update appeal window for the round
        com.fpt.swp.sealhackathonbe.round.entity.Round round = existingRankings.get(0).getRound();
        round.setAppealStartTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        if (appealDurationMinutes != null) {
            round.setAppealEndTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMinutes(appealDurationMinutes));
        } else {
            round.setAppealEndTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMinutes(40)); // default 40 mins
        }
        
        List<UUID> recipientIds = new ArrayList<>();
        for (RoundRanking r : existingRankings) {
            r.setIsPublished(true);
            
            List<com.fpt.swp.sealhackathonbe.team.entity.TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(r.getTeam().getTeamId());
            members.forEach(m -> recipientIds.add(m.getUserId()));

            // We no longer automatically disqualify teams that do not advance.
            // Not advancing is purely tracked by the isAdvanced flag.
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
    public void approveRoundRankings(UUID roundId, UUID categoryId, UUID adminUserId) {
        List<RoundRanking> existingRankings = roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Rankings must be computed before approving.");
        }
        
        com.fpt.swp.sealhackathonbe.round.entity.Round round = existingRankings.get(0).getRound();
        
        // Kiểm tra thời gian appeal đã đóng chưa
        if (round.getAppealEndTime() != null && java.time.LocalDateTime.now().isBefore(round.getAppealEndTime())) {
            throw new IllegalStateException("Cannot approve rankings while the appeal window is still open.");
        }
        
        // Kiểm tra còn appeal nào đang pending không
        if (appealRepository.existsByRound_RoundIdAndStatus(roundId, AppealStatus.PENDING)) {
            throw new IllegalStateException("Cannot approve rankings because there are still pending appeals for this round.");
        }
        
        validateScoresFinalizedForRound(roundId, "approve");
        for (RoundRanking r : existingRankings) {
            r.setIsApproved(true);
        }
        roundRankingRepository.saveAll(existingRankings);
    }

    @Override
    @Transactional
    public List<EventRankingDTO> computeEventRankings(UUID eventId) {
        Event eventRef = entityManager.find(Event.class, eventId);
        if (eventRef == null) throw new IllegalArgumentException("Event ID does not exist: " + eventId);
        
        List<Category> categories = categoryRepository.findByEventEventIdAndIsActiveTrueOrderBySortOrderAsc(eventId);
        List<EventRankingDTO> allComputedRankings = new ArrayList<>();

        for (Category categoryRef : categories) {
            allComputedRankings.addAll(computeCategoryEventRankings(categoryRef.getCategoryId()));
        }

        return allComputedRankings;
    }

    @Override
    @Transactional
    public List<EventRankingDTO> computeCategoryEventRankings(UUID categoryId) {
        Category categoryRef = entityManager.find(Category.class, categoryId);
        if (categoryRef == null) throw new IllegalArgumentException("Category ID does not exist: " + categoryId);
        UUID eventId = categoryRef.getEvent().getEventId();
        List<EventRanking> rankings = new ArrayList<>();
        
        RoundResponse finalRound = null;
        try {
            finalRound = roundService.getFinalRound(categoryId);
        } catch (RuntimeException e) {
            log.warn("Skipping Event Ranking computation for Category {} because it has no rounds.", categoryRef.getCategoryName());
            return Collections.emptyList();
        }

        if (finalRound == null) {
            log.warn("Skipping Event Ranking computation for Category {} because final round is null.", categoryRef.getCategoryName());
            return Collections.emptyList();
        }

        if (Boolean.TRUE.equals(finalRound.getIsCalibrationRound())) {
            throw new IllegalStateException("Cannot compute event rankings because the final round is a calibration round.");
        }

        List<UUID> teamIds = entityManager.createQuery(
                "SELECT t.teamId FROM Teams t WHERE t.category.categoryId = :categoryId AND t.event.eventId = :eventId", UUID.class)
                .setParameter("categoryId", categoryId)
                .setParameter("eventId", eventId)
                .getResultList();

        List<UUID> disqualifiedTeamIds = teamDisqualificationService.getDisqualifiedTeamsByCategory(categoryId)
                .stream().map(com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse::getTeamId).collect(Collectors.toList());
        
        List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
        if (existingRankings.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsApproved()))) {
            throw new IllegalStateException("Cannot compute rankings because they have been approved and locked.");
        }

        Map<UUID, EventRanking> existingRankingMap = existingRankings.stream()
                .collect(Collectors.toMap(r -> r.getTeam().getTeamId(), r -> r));

        List<RoundRanking> allRoundRankings = roundRankingRepository.findByCategory_CategoryId(categoryId);

        UUID finalRoundId = finalRound.getRoundId();
        List<RoundRanking> finalRoundRankings = allRoundRankings.stream()
                .filter(r -> r.getRound().getRoundId().equals(finalRoundId) && teamIds.contains(r.getTeam().getTeamId()))
                .collect(Collectors.toList());

        if (finalRoundRankings.isEmpty() && !teamIds.isEmpty()) {
            log.warn("Skipping Event Ranking computation for Category {} because final round rankings have not been computed.", categoryRef.getCategoryName());
            return Collections.emptyList();
        }

        // KIỂM TRA TẤT CẢ RANKING CỦA CÁC VÒNG ĐÃ ĐƯỢC DUYỆT CHƯA
        boolean allRoundsApproved = allRoundRankings.stream()
                .allMatch(r -> Boolean.TRUE.equals(r.getIsApproved()));
        if (!allRoundRankings.isEmpty() && !allRoundsApproved) {
            throw new IllegalStateException("Cannot compute event rankings because not all round rankings in this category have been approved.");
        }

        allRoundRankings.sort(java.util.Comparator.comparing(r -> r.getRound().getRoundOrder()));

        Map<UUID, BigDecimal> dScores = new java.util.HashMap<>();
        Map<UUID, Integer> dRoundOrders = new java.util.HashMap<>();
        Map<UUID, LocalDateTime> dSubmissionTimes = new java.util.HashMap<>();
        for (RoundRanking rr : allRoundRankings) {
            dScores.put(rr.getTeam().getTeamId(), rr.getAverageScore() != null ? rr.getAverageScore() : BigDecimal.ZERO);
            dRoundOrders.put(rr.getTeam().getTeamId(), rr.getRound().getRoundOrder());
            dSubmissionTimes.put(rr.getTeam().getTeamId(), rr.getSubmission().getLastUpdatedAt());
        }

        // Tối ưu N+1: Truy vấn tất cả Team Names của các đội trong bảng đấu bằng 1 câu lệnh IN duy nhất
        Map<UUID, String> teamNameMap = Collections.emptyMap();
        if (!teamIds.isEmpty()) {
            List<Teams> teams = entityManager.createQuery(
                    "SELECT t FROM Teams t WHERE t.teamId IN :teamIds", Teams.class)
                    .setParameter("teamIds", teamIds)
                    .getResultList();
            teamNameMap = teams.stream().collect(Collectors.toMap(Teams::getTeamId, Teams::getTeamName));
        }

        for (UUID teamId : teamIds) {
            Teams teamRef = entityManager.getReference(Teams.class, teamId);
            BigDecimal finalScore = BigDecimal.ZERO;

            if (!disqualifiedTeamIds.contains(teamId)) {
                finalScore = dScores.getOrDefault(teamId, BigDecimal.ZERO);
            }

            EventRanking ranking = existingRankingMap.getOrDefault(teamId, new EventRanking());
            ranking.setEvent(categoryRef.getEvent());
            ranking.setCategory(categoryRef);
            ranking.setTeam(teamRef);
            ranking.setFinalScore(finalScore);
            ranking.setRankPosition(0);
            ranking.setIsPublished(false);
            ranking.setIsApproved(false);

            rankings.add(ranking);
        }

        rankings.sort((r1, r2) -> {
            int roundOrder1 = dRoundOrders.getOrDefault(r1.getTeam().getTeamId(), -1);
            int roundOrder2 = dRoundOrders.getOrDefault(r2.getTeam().getTeamId(), -1);
            
            if (disqualifiedTeamIds.contains(r1.getTeam().getTeamId())) roundOrder1 = -2;
            if (disqualifiedTeamIds.contains(r2.getTeam().getTeamId())) roundOrder2 = -2;

            if (roundOrder1 != roundOrder2) {
                return Integer.compare(roundOrder2, roundOrder1);
            }
            int scoreCompare = r2.getFinalScore().compareTo(r1.getFinalScore());
            if (scoreCompare != 0) return scoreCompare;
            
            LocalDateTime t1 = dSubmissionTimes.get(r1.getTeam().getTeamId());
            LocalDateTime t2 = dSubmissionTimes.get(r2.getTeam().getTeamId());
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        });

        int currentRank = 1;
        int validRankCount = 0;
        EventRanking prevValidTeam = null;
        int prevValidRoundOrder = -1;

        for (int i = 0; i < rankings.size(); i++) {
            EventRanking currentTeam = rankings.get(i);

            boolean isCurrentDisqualified = disqualifiedTeamIds.contains(currentTeam.getTeam().getTeamId());
            boolean hasZeroScore = currentTeam.getFinalScore().compareTo(BigDecimal.ZERO) == 0;

            if (isCurrentDisqualified || hasZeroScore) {
                currentTeam.setRankPosition(0);
                continue;
            }

            if (validRankCount > 0 && prevValidTeam != null) {
                int currentRoundOrder = dRoundOrders.getOrDefault(currentTeam.getTeam().getTeamId(), -1);
                if (currentRoundOrder < prevValidRoundOrder) {
                    currentRank = validRankCount + 1;
                } else if (currentRoundOrder == prevValidRoundOrder) {
                    int scoreCompare = currentTeam.getFinalScore().compareTo(prevValidTeam.getFinalScore());
                    if (scoreCompare < 0) {
                        currentRank = validRankCount + 1;
                    } else if (scoreCompare == 0) {
                        LocalDateTime t1 = dSubmissionTimes.get(prevValidTeam.getTeam().getTeamId());
                        LocalDateTime t2 = currentTeam.getTeam().getTeamId() != null ? dSubmissionTimes.get(currentTeam.getTeam().getTeamId()) : null;
                        if (t1 != null && t2 != null && t2.isAfter(t1)) {
                            currentRank = validRankCount + 1;
                        }
                    }
                }
            }
            currentTeam.setRankPosition(currentRank);
            validRankCount++;
            prevValidTeam = currentTeam;
            prevValidRoundOrder = dRoundOrders.getOrDefault(currentTeam.getTeam().getTeamId(), -1);
        }

        List<EventRanking> savedRankings = eventRankingRepository.saveAll(rankings);

        final Map<UUID, String> finalTeamNameMap = teamNameMap;
        return savedRankings.stream().map(r -> EventRankingDTO.builder()
                .id(r.getId())
                .eventId(r.getEvent().getEventId())
                .categoryId(r.getCategory().getCategoryId())
                .teamId(r.getTeam().getTeamId())
                .teamName(finalTeamNameMap.get(r.getTeam().getTeamId()))
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .isApproved(r.getIsApproved())
                .build()
        ).sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void publishEventRankings(UUID eventId, UUID categoryId) {
        if (categoryId != null) {
            publishCategoryEventRankings(categoryId, null);
            return;
        }
        List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventId(eventId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Event rankings must be computed before publishing.");
        }
        for (EventRanking r : existingRankings) {
            r.setIsPublished(true);
        }
        eventRankingRepository.saveAll(existingRankings);
    }

    @Override
    @Transactional
    public void publishCategoryEventRankings(UUID categoryId, UUID adminUserId) {
        Category categoryRef = entityManager.find(Category.class, categoryId);
        if (categoryRef == null) throw new IllegalArgumentException("Category ID does not exist: " + categoryId);
        UUID eventId = categoryRef.getEvent().getEventId();
        
        List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Category rankings must be computed before publishing.");
        }
        for (EventRanking r : existingRankings) {
            r.setIsPublished(true);
        }
        eventRankingRepository.saveAll(existingRankings);
    }

    @Override
    @Transactional
    public void approveCategoryEventRankings(UUID categoryId, UUID adminUserId) {
        Category categoryRef = entityManager.find(Category.class, categoryId);
        if (categoryRef == null) throw new IllegalArgumentException("Category ID does not exist: " + categoryId);
        UUID eventId = categoryRef.getEvent().getEventId();
        
        List<EventRanking> existingRankings = eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId);
        if (existingRankings.isEmpty()) {
            throw new IllegalStateException("Category rankings must be computed before approving.");
        }
        for (EventRanking r : existingRankings) {
            r.setIsApproved(true);
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
                .categoryName(r.getCategory().getCategoryName())
                .teamId(r.getTeam().getTeamId())
                .teamName(r.getTeam().getTeamName())
                .finalScore(r.getFinalScore())
                .rankPosition(r.getRankPosition())
                .computedAt(r.getComputedAt() != null ? r.getComputedAt() : LocalDateTime.now())
                .isPublished(r.getIsPublished())
                .isApproved(r.getIsApproved())
                .build()
        )
        .sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE))
        .collect(Collectors.toList());
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
                .sorted((r1, r2) -> {
                    int pos1 = r1.getRankPosition() > 0 ? r1.getRankPosition() : Integer.MAX_VALUE;
                    int pos2 = r2.getRankPosition() > 0 ? r2.getRankPosition() : Integer.MAX_VALUE;
                    return Integer.compare(pos1, pos2);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRankingDTO> getApprovedCategoryLeaderboard(UUID eventId, UUID categoryId) {
        List<EventRankingDTO> rankings = getCategoryLeaderboard(eventId, categoryId);
        if (rankings.isEmpty()) {
            return rankings;
        }
        boolean isApproved = rankings.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsApproved()));
        if (!isApproved) {
            throw new IllegalStateException("Leaderboard has not been approved yet.");
        }
        return rankings.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsApproved()))
                .sorted((r1, r2) -> {
                    int pos1 = r1.getRankPosition() > 0 ? r1.getRankPosition() : Integer.MAX_VALUE;
                    int pos2 = r2.getRankPosition() > 0 ? r2.getRankPosition() : Integer.MAX_VALUE;
                    return Integer.compare(pos1, pos2);
                })
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
                .sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE))
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
                .isApproved(r.getIsApproved())
                .build()
        ).sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE)).collect(Collectors.toList());
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
                .isApproved(r.getIsApproved())
                .build()
        ).sorted(Comparator.comparingInt(r -> r.getRankPosition() > 0 ? r.getRankPosition() : Integer.MAX_VALUE)).collect(Collectors.toList());
    }

    private void validateScoresFinalizedForRound(UUID roundId, String action) {
        List<SubmissionResponse> submissionsList = submissionQueryService.getSubmissionsByRound(roundId);
        List<UUID> disqualifiedSubIds = submissionDisqualificationService.getDisqualifiedSubmissions(roundId).stream()
                .map(DisqualifiedSubmissionResponse::getSubmissionId)
                .toList();
        validateScoresFinalizedForRound(submissionsList, disqualifiedSubIds, action);
    }

    private void validateScoresFinalizedForRound(List<SubmissionResponse> submissionsList, List<UUID> disqualifiedSubIds, String action) {
        boolean allScoresApproved = submissionsList.stream()
                .filter(sub -> !disqualifiedSubIds.contains(sub.getSubmissionId()))
                .allMatch(sub -> Boolean.TRUE.equals(sub.getIsScoreApproved()));

        if (!submissionsList.isEmpty() && !allScoresApproved) {
            throw new IllegalStateException(String.format("Cannot %s rankings because not all valid submissions have their scores finalized and approved.", action));
        }
    }
}
