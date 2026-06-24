package com.fpt.swp.sealhackathonbe.award.service.impl;

import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternItemRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternResponse;
import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
import com.fpt.swp.sealhackathonbe.award.dto.RankingAwardCandidateResponse;
import com.fpt.swp.sealhackathonbe.award.entity.Award;
import com.fpt.swp.sealhackathonbe.award.entity.AwardPattern;
import com.fpt.swp.sealhackathonbe.award.entity.AwardTier;
import com.fpt.swp.sealhackathonbe.award.repository.AwardPatternRepository;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.award.repository.AwardTierRepository;
import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final AwardTierRepository awardTierRepository;
    private final AwardPatternRepository awardPatternRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;
    private final RoundRepository roundRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final NotificationService notificationService;
    private final TeamMembersRepository teamMembersRepository;

    @Override
    @Transactional
    public AwardResponse grantAward(AwardRequest request, UUID adminId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + request.getEventId()));

        Teams team = teamsRepository.findById(request.getTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + request.getTeamId()));

        AwardTier tier = awardTierRepository.findById(request.getAwardTierId())
                .orElseThrow(() -> new EntityNotFoundException("Invalid award tier."));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Executor account does not exist."));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found."));
        }

        Award award = new Award();
        award.setEvent(event);
        award.setCategory(category);
        award.setTeam(team);
        award.setAwardTier(tier);
        award.setAwardTitle(request.getAwardTitle());
        award.setDescription(request.getDescription());
        award.setPrizeValue(request.getPrizeValue());
        award.setPrizeCurrency(defaultCurrency(request.getPrizeCurrency()));
        award.setAwardedAt(Instant.now());
        award.setAwardedBy(admin);
        award.setIsPublished(true);
        award.setPublishedAt(Instant.now());

        Award savedAward = awardRepository.save(award);
        notifyTeamAboutAward(savedAward, adminId);
        return convertToResponse(savedAward);
    }

    @Override
    @Transactional(readOnly = true)
    public AwardResponse getAwardById(UUID awardId) {
        Award award = awardRepository.findById(awardId)
                .orElseThrow(() -> new EntityNotFoundException("Award data not found."));
        return convertToResponse(award);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwardResponse> getAwardsByEvent(UUID eventId) {
        return awardRepository.findAllByEventEventId(eventId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallOfFameResponse> getHallOfFameData() {
        return awardRepository.findByIsPublishedTrueOrderByAwardedAtDesc().stream()
                .map(award -> {
                    HallOfFameResponse response = new HallOfFameResponse();
                    response.setEventName(award.getEvent().getEventName());
                    response.setCategoryName(award.getCategory() != null
                            ? award.getCategory().getCategoryName()
                            : "Event-wide Award");
                    response.setTeamName(award.getTeam().getTeamName());
                    response.setAwardTierName(award.getAwardTier().getTierName());
                    response.setAwardTitle(award.getAwardTitle());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AwardPatternResponse> saveAwardPatterns(UUID categoryId, AwardPatternRequest request) {
        Category category = getCategory(categoryId);
        Event event = category.getEvent();

        Set<Integer> requestedRanks = new HashSet<>();
        for (AwardPatternItemRequest item : request.getPatterns()) {
            if (!requestedRanks.add(item.getRankPosition())) {
                throw new IllegalArgumentException("Duplicate rank position: " + item.getRankPosition());
            }
        }

        List<AwardPattern> currentPatterns = awardPatternRepository
                .findByCategoryCategoryIdAndIsActiveTrueOrderByRankPositionAsc(categoryId);
        for (AwardPattern pattern : currentPatterns) {
            if (!requestedRanks.contains(pattern.getRankPosition())) {
                pattern.setIsActive(false);
            }
        }

        for (AwardPatternItemRequest item : request.getPatterns()) {
            AwardTier tier = awardTierRepository.findById(item.getAwardTierId())
                    .orElseThrow(() -> new EntityNotFoundException("Invalid award tier for rank " + item.getRankPosition()));

            AwardPattern pattern = awardPatternRepository
                    .findByCategoryCategoryIdAndRankPosition(categoryId, item.getRankPosition())
                    .orElseGet(AwardPattern::new);

            pattern.setEvent(event);
            pattern.setCategory(category);
            pattern.setRankPosition(item.getRankPosition());
            pattern.setAwardTier(tier);
            pattern.setAwardTitle(item.getAwardTitle());
            pattern.setDescription(item.getDescription());
            pattern.setPrizeValue(item.getPrizeValue());
            pattern.setPrizeCurrency(defaultCurrency(item.getPrizeCurrency()));
            pattern.setIsActive(true);

            awardPatternRepository.save(pattern);
        }

        return getAwardPatterns(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwardPatternResponse> getAwardPatterns(UUID categoryId) {
        return awardPatternRepository.findByCategoryCategoryIdAndIsActiveTrueOrderByRankPositionAsc(categoryId).stream()
                .map(this::convertToPatternResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingAwardCandidateResponse> getTopRankingByCategory(UUID categoryId, UUID roundId, int limit) {
        Round round = resolveRound(categoryId, roundId);
        return getTopRankings(round.getRoundId(), categoryId, limit).stream()
                .map(this::convertToRankingCandidate)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AwardResponse> autoGrantTopAwards(UUID categoryId, UUID roundId, UUID adminId, int limit) {
        Category category = getCategory(categoryId);
        Event event = category.getEvent();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Executor account does not exist."));
        Round round = resolveRound(categoryId, roundId);

        List<RoundRanking> rankings = getTopRankings(round.getRoundId(), categoryId, limit);
        if (rankings.isEmpty()) {
            throw new IllegalStateException("No ranking exists for this category/round.");
        }

        Map<Integer, AwardPattern> patternByRank = awardPatternRepository
                .findByCategoryCategoryIdAndIsActiveTrueOrderByRankPositionAsc(categoryId).stream()
                .collect(Collectors.toMap(
                        AwardPattern::getRankPosition,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        validateAwardPatternsExist(rankings, patternByRank);

        return rankings.stream()
                .map(ranking -> grantRankingAward(event, category, admin, ranking, patternByRank))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Award grantRankingAward(
            Event event,
            Category category,
            User admin,
            RoundRanking ranking,
            Map<Integer, AwardPattern> patternByRank
    ) {
        AwardPattern pattern = patternByRank.get(ranking.getRankPosition());
        if (pattern == null) {
            throw new IllegalStateException("Award pattern has not been configured for rank " + ranking.getRankPosition());
        }

        Teams team = ranking.getTeam();
        Award award = awardRepository
                .findByEventEventIdAndCategoryCategoryIdAndTeamTeamIdAndAwardTitle(
                        event.getEventId(),
                        category.getCategoryId(),
                        team.getTeamId(),
                        pattern.getAwardTitle()
                )
                .orElseGet(Award::new);

        award.setEvent(event);
        award.setCategory(category);
        award.setTeam(team);
        award.setAwardTier(pattern.getAwardTier());
        award.setAwardTitle(pattern.getAwardTitle());
        award.setDescription(pattern.getDescription());
        award.setPrizeValue(pattern.getPrizeValue());
        award.setPrizeCurrency(defaultCurrency(pattern.getPrizeCurrency()));
        award.setAwardedAt(Instant.now());
        award.setAwardedBy(admin);
        award.setIsPublished(true);
        award.setPublishedAt(Instant.now());

        Award savedAward = awardRepository.save(award);
        notifyTeamAboutAward(savedAward, admin.getUserId());
        return savedAward;
    }

    private void notifyTeamAboutAward(Award award, UUID adminId) {
        List<UUID> recipientIds = teamMembersRepository.findByTeamIdAndActiveTrue(award.getTeam().getTeamId())
                .stream()
                .map(TeamMembers::getUserId)
                .collect(Collectors.toList());

        if (recipientIds.isEmpty()) return;

        String title = "Congratulations! Your team won an award: " + award.getAwardTitle();
        String body = String.format("Hello %s,\n\n" +
                "We are thrilled to announce that your team has been granted the \"%s - %s\" award in the event \"%s\".\n\n" +
                "Prize: %s %s\n\n" +
                "Congratulations from the organization board!",
                award.getTeam().getTeamName(),
                award.getAwardTier().getTierName(),
                award.getAwardTitle(),
                award.getEvent().getEventName(),
                award.getPrizeValue() != null ? award.getPrizeValue().toString() : "N/A",
                award.getPrizeCurrency()
        );

        notificationService.sendBroadcastNotification(
                recipientIds,
                adminId,
                award.getEvent().getEventId(),
                title,
                body
        );
    }

    private Category getCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
    }

    private Round resolveRound(UUID categoryId, UUID roundId) {
        if (roundId != null) {
            Round round = roundRepository.findById(roundId)
                    .orElseThrow(() -> new EntityNotFoundException("Round not found with ID: " + roundId));
            if (!round.getCategory().getCategoryId().equals(categoryId)) {
                throw new IllegalArgumentException("Round does not belong to the selected category.");
            }
            return round;
        }

        return roundRepository.findTopByCategoryCategoryIdOrderByRoundOrderDesc(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("This category has no round available for ranking retrieval."));
    }

    private List<RoundRanking> getTopRankings(UUID roundId, UUID categoryId, int limit) {
        validateLimit(limit);
        return roundRankingRepository
                .findByRoundRoundIdAndCategoryCategoryIdOrderByRankPositionAsc(
                        roundId,
                        categoryId,
                        PageRequest.of(0, limit)
                );
    }

    private void validateLimit(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Award limit must be greater than 0.");
        }
    }

    private void validateAwardPatternsExist(
            List<RoundRanking> rankings,
            Map<Integer, AwardPattern> patternByRank
    ) {
        List<Integer> missingRanks = rankings.stream()
                .map(RoundRanking::getRankPosition)
                .filter(rank -> !patternByRank.containsKey(rank))
                .collect(Collectors.toList());

        if (!missingRanks.isEmpty()) {
            throw new IllegalStateException("Award pattern has not been configured for ranks: " + missingRanks);
        }
    }

    private AwardResponse convertToResponse(Award award) {
        AwardResponse response = new AwardResponse();
        response.setId(award.getId());
        response.setEventId(award.getEvent().getEventId());
        response.setEventName(award.getEvent().getEventName());
        response.setTeamId(award.getTeam().getTeamId());
        response.setTeamName(award.getTeam().getTeamName());
        response.setAwardTierId(award.getAwardTier().getId());
        response.setAwardTierName(award.getAwardTier().getTierName());
        response.setAwardTitle(award.getAwardTitle());
        response.setDescription(award.getDescription());
        response.setPrizeValue(award.getPrizeValue());
        response.setPrizeCurrency(award.getPrizeCurrency());
        response.setAwardedAt(award.getAwardedAt());
        response.setAwardedByName(award.getAwardedBy().getFullName());
        response.setIsPublished(award.getIsPublished());
        response.setPublishedAt(award.getPublishedAt());

        if (award.getCategory() != null) {
            response.setCategoryId(award.getCategory().getCategoryId());
            response.setCategoryName(award.getCategory().getCategoryName());
        }
        return response;
    }

    private AwardPatternResponse convertToPatternResponse(AwardPattern pattern) {
        AwardPatternResponse response = new AwardPatternResponse();
        response.setId(pattern.getId());
        response.setEventId(pattern.getEvent().getEventId());
        response.setEventName(pattern.getEvent().getEventName());
        response.setCategoryId(pattern.getCategory().getCategoryId());
        response.setCategoryName(pattern.getCategory().getCategoryName());
        response.setRankPosition(pattern.getRankPosition());
        response.setAwardTierId(pattern.getAwardTier().getId());
        response.setAwardTierName(pattern.getAwardTier().getTierName());
        response.setAwardTitle(pattern.getAwardTitle());
        response.setDescription(pattern.getDescription());
        response.setPrizeValue(pattern.getPrizeValue());
        response.setPrizeCurrency(pattern.getPrizeCurrency());
        response.setIsActive(pattern.getIsActive());
        return response;
    }

    private RankingAwardCandidateResponse convertToRankingCandidate(RoundRanking ranking) {
        RankingAwardCandidateResponse response = new RankingAwardCandidateResponse();
        response.setRankingId(ranking.getId());
        response.setRoundId(ranking.getRound().getRoundId());
        response.setRoundName(ranking.getRound().getRoundName());
        response.setCategoryId(ranking.getCategory().getCategoryId());
        response.setCategoryName(ranking.getCategory().getCategoryName());
        response.setTeamId(ranking.getTeam().getTeamId());
        response.setTeamName(ranking.getTeam().getTeamName());
        response.setTotalScore(ranking.getTotalScore());
        response.setAverageScore(ranking.getAverageScore());
        response.setRankPosition(ranking.getRankPosition());
        response.setIsAdvanced(ranking.getIsAdvanced());
        return response;
    }

    private String defaultCurrency(String currency) {
        return currency != null && !currency.isBlank() ? currency : "VND";
    }
}
