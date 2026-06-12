package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundStatusRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {
    private final CategoryRepository categoryRepository;
    private final RoundRepository roundRepository;
    private final RoundStatusRepository roundStatusRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final RoundMapper roundMapper;

    @Override
    public RoundResponse create(CreateRoundRequest request) {
        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        RoundStatus status = roundStatusRepository
                .findById(request.getRoundStatusId())
                .orElseThrow(() -> new RuntimeException("Round status not found"));
        int currentRound = roundRepository.findMaxRoundOrderByCategory(request.getCategoryId());
        int nextRound = currentRound + 1;
        Round round = Round.builder()
                .roundId(UUID.randomUUID())
                .category(category)
                .roundName(request.getRoundName())
                .description(request.getDescription())
                .roundOrder(nextRound)
                .roundStatus(status)
                .submissionDeadline(request.getSubmissionDeadline())
                .judgingDeadline(request.getJudgingDeadline())
                .startDate(request.getStartDate())
                .endDate(request.getStartDate())
                .advancementTopN(request.getAdvancementTopN())
                .isCalibrationRound(request.getIsCalibrationRound())
                .build();
        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse getById(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
        return roundMapper.toRoundResponse(round);
    }

    @Override
    public List<RoundResponse> getByCategory(UUID categoryId) {
        return roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId)
                .stream()
                .map(roundMapper::toRoundResponse)
                .toList();
    }

    @Override
    public RoundResponse update(UUID roundId, UpdateRoundRequest request) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
        RoundStatus roundStatus = roundStatusRepository.findById(request.getRoundStatusId())
                        .orElseThrow(() -> new RuntimeException("Round status not found"));

        round.setRoundName(request.getRoundName());
        round.setDescription(request.getDescription());
        round.setRoundOrder(request.getRoundOrder());
        round.setRoundStatus(roundStatus);
        round.setStartDate(request.getStartDate());
        round.setEndDate(request.getEndDate());
        round.setSubmissionDeadline(request.getSubmissionDeadline());
        round.setJudgingDeadline(request.getJudgingDeadline());
        round.setAdvancementTopN(request.getAdvancementTopN());
        round.setIsCalibrationRound(request.getIsCalibrationRound());

        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    public void delete(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));

        if (!"Upcoming".equalsIgnoreCase(round.getRoundStatus().getStatusName())) {
            throw new IllegalArgumentException("Only delete round at status 'Upcoming'");
        }

        boolean hasJudge = roundJudgeRepository.existsByRoundRoundId(roundId);
        if (hasJudge) {
            throw new IllegalArgumentException("Cannot delete round assigned judge");
        }

        roundRepository.delete(round);
    }

    @Override
    public RoundResponse getFinalRound(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found");
        }
        return roundRepository.findTopByCategoryCategoryIdOrderByRoundOrderDesc(categoryId)
                .map(roundMapper::toRoundResponse)
                .orElseThrow(() -> new RuntimeException("No any rounds in this category"));
    }

    @Override
    public Integer getAdvancementTopN(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
        return round.getAdvancementTopN();
    }


}
