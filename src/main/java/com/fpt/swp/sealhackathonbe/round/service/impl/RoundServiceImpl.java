package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {
    private final CategoryRepository categoryRepository;
    private final RoundRepository roundRepository;
    private final RoundStatusRepository roundStatusRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final RoundMapper roundMapper;

    @Override
    public RoundResponse create(UUID categoryId, CreateRoundRequest request) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        RoundStatus status = roundStatusRepository
                .findById(request.getRoundStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Round status not found"));
        int currentRound = roundRepository.findMaxRoundOrderByCategory(categoryId);
        int nextRound = currentRound + 1;

        validateRoundTimeline(
                request.getStartDate(),
                request.getEndDate(),
                request.getSubmissionDeadline(),
                request.getJudgingDeadline(),
                category.getEvent()
        );

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
                .endDate(request.getEndDate())
                .advancementTopN(request.getAdvancementTopN())
                .isCalibrationRound(request.getIsCalibrationRound())
                .build();
        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse getById(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
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
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        RoundStatus roundStatus = roundStatusRepository.findById(request.getRoundStatusId())
                        .orElseThrow(() -> new EntityNotFoundException("Round status not found"));
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

        validateRoundTimeline(
                round.getStartDate(),
                round.getEndDate(),
                round.getSubmissionDeadline(),
                round.getJudgingDeadline(),
                round.getCategory().getEvent()
        );

        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    public void delete(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));

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
            throw new EntityNotFoundException("Category not found");
        }
        return roundRepository.findTopByCategoryCategoryIdOrderByRoundOrderDesc(categoryId)
                .map(roundMapper::toRoundResponse)
                .orElseThrow(() -> new RuntimeException("No any rounds in this category"));
    }

    @Override
    public Integer getAdvancementTopN(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        return round.getAdvancementTopN();
    }

    private void validateRoundTimeline(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime submissionDeadline, LocalDateTime judgingDeadline, Event event) {
        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new BadRequestException("Start date must be strictly before end date");
        }

        if (event != null) {
            LocalDateTime earliestAllowed = event.getEventStartDate().atStartOfDay();
            LocalDateTime latestAllowed = event.getEventEndDate().atTime(LocalTime.MAX);

            if (startDate != null && startDate.isBefore(earliestAllowed)) {
                throw new BadRequestException("Round start date cannot be before event start date");
            }
            if (endDate != null && endDate.isAfter(latestAllowed)) {
                throw new BadRequestException("Round end date cannot be after event end date");
            }
        }

        if (submissionDeadline != null) {
            if (startDate != null && submissionDeadline.isBefore(startDate)) {
                throw new BadRequestException("Submission deadline must be after or equal to start date");
            }
            if (endDate != null && submissionDeadline.isAfter(endDate)) {
                throw new BadRequestException("Submission deadline must be before or equal to end date");
            }
        }
        if (judgingDeadline != null) {
            if (submissionDeadline != null && judgingDeadline.isBefore(submissionDeadline)) {
                throw new BadRequestException("Judging deadline must be after or equal to submission deadline");
            } else if (startDate != null && judgingDeadline.isBefore(startDate)) {
                throw new BadRequestException("Judging deadline must be after or equal to start date");
            }
            
            if (endDate != null && judgingDeadline.isAfter(endDate)) {
                throw new BadRequestException("Judging deadline must be before or equal to end date");
            }
        }
    }

}
