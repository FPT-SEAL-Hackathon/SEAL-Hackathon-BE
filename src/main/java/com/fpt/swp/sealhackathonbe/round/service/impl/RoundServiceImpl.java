package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundStatusRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JudgingRepository judgingRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    private final SubmissionsRepository submissionsRepository;
    private final SubmissionHistoryRepository submissionHistoryRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final RoundMapper roundMapper;

    @Override
    @Transactional
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
                request.getAppealStartTime(),
                request.getAppealEndTime(),
                category.getEvent());

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
                .appealStartTime(request.getAppealStartTime())
                .appealEndTime(request.getAppealEndTime())
                .advancementTopN(request.getAdvancementTopN())
                .isCalibrationRound(request.getIsCalibrationRound())
                .build();
        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getById(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        return roundMapper.toRoundResponse(round);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> getByCategory(UUID categoryId) {
        return roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId)
                .stream()
                .map(roundMapper::toRoundResponse)
                .toList();
    }

    @Override
    @Transactional
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
        round.setAppealStartTime(request.getAppealStartTime());
        round.setAppealEndTime(request.getAppealEndTime());
        round.setAdvancementTopN(request.getAdvancementTopN());
        round.setIsCalibrationRound(request.getIsCalibrationRound());

        validateRoundTimeline(
                round.getStartDate(),
                round.getEndDate(),
                round.getSubmissionDeadline(),
                round.getJudgingDeadline(),
                round.getAppealStartTime(),
                round.getAppealEndTime(),
                round.getCategory().getEvent());

        return roundMapper.toRoundResponse(roundRepository.save(round));
    }

    @Override
    @Transactional
    public void delete(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));

        if (!"Upcoming".equalsIgnoreCase(round.getRoundStatus().getStatusName())) {
            throw new BusinessConflictException("Only delete round at status 'Upcoming'");
        }

        ensureRoundHasNoDeleteBlockers(roundId);
        roundRepository.delete(round);
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getFinalRound(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found");
        }
        return roundRepository.findTopByCategoryCategoryIdOrderByRoundOrderDesc(categoryId)
                .map(roundMapper::toRoundResponse)
                .orElseThrow(() -> new RuntimeException("No any rounds in this category"));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAdvancementTopN(UUID roundId) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        return round.getAdvancementTopN();
    }

    private void validateRoundTimeline(
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime submissionDeadline,
            LocalDateTime judgingDeadline,
            LocalDateTime appealStartTime,
            LocalDateTime appealEndTime,
            Event event) {
        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            throw new BadRequestException("Start date must be strictly before end date");
        }

        if (event != null) {
            LocalDateTime earliestAllowed = event.getEventStartDate();
            LocalDateTime latestAllowed = event.getEventEndDate();

            // Event dates are date-only: rounds and appeal windows may use any minute
            // inside those calendar days.
            if (startDate != null && startDate.isBefore(earliestAllowed)) {
                throw new BadRequestException("Round start date cannot be before event start date");
            }
            if (endDate != null && endDate.isAfter(latestAllowed)) {
                throw new BadRequestException("Round end date cannot be after event end date");
            }
            if (appealStartTime != null && appealStartTime.isBefore(earliestAllowed)) {
                throw new BadRequestException("Appeal start time cannot be before event start date");
            }
            if (appealEndTime != null && appealEndTime.isAfter(latestAllowed)) {
                throw new BadRequestException("Appeal end time cannot be after event end date");
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

        if (appealStartTime != null) {
            if (judgingDeadline != null && appealStartTime.isBefore(judgingDeadline)) {
                throw new BadRequestException("Appeal start time must be after or equal to judging deadline");
            } else if (startDate != null && appealStartTime.isBefore(startDate)) {
                throw new BadRequestException("Appeal start time must be after or equal to start date");
            }
        }

        if (appealEndTime != null && endDate != null && appealEndTime.isAfter(endDate)) {
            throw new BadRequestException("Appeal end time must be before or equal to end date");
        }

        if (appealStartTime != null && appealEndTime != null && !appealStartTime.isBefore(appealEndTime)) {
            throw new BadRequestException("Appeal start time must be strictly before appeal end time");
        }
    }

    private void ensureRoundHasNoDeleteBlockers(UUID roundId) {
        // Round delete is a hard delete, so every FK-backed setup/result record must be
        // removed first.
        if (roundCriterionRepository.existsByRoundRoundId(roundId)) {
            throw new BusinessConflictException(
                    "Cannot delete round because it already has criteria. Remove criteria first.");
        }
        removeInactiveJudgeAssignmentsOrBlock(roundId);
        if (submissionsRepository.existsByRoundId(roundId)) {
            throw new BusinessConflictException("Cannot delete round because it already has submissions.");
        }
        if (submissionHistoryRepository.existsByRoundId(roundId)) {
            throw new BusinessConflictException("Cannot delete round because it already has submission history.");
        }
        if (roundRankingRepository.existsByRoundRoundId(roundId)) {
            throw new BusinessConflictException("Cannot delete round because it already has rankings.");
        }
        if (roundRepository.countCalibrationSamplesByRoundId(roundId) > 0) {
            throw new BusinessConflictException("Cannot delete round because it already has calibration samples.");
        }
    }

    private void removeInactiveJudgeAssignmentsOrBlock(UUID roundId) {
        List<RoundJudge> inactiveAssignments = new java.util.ArrayList<>();
        for (RoundJudge roundJudge : roundJudgeRepository.findByRoundRoundId(roundId)) {
            if (Boolean.TRUE.equals(roundJudge.getIsActive())) {
                throw new BusinessConflictException("Cannot delete round because it already has assigned judges.");
            }
            if (judgingRepository.existsByRoundJudge_RoundJudgeId(roundJudge.getRoundJudgeId())) {
                throw new BusinessConflictException(
                        "Cannot delete round because it already has judge scoring history.");
            }
            inactiveAssignments.add(roundJudge);
        }

        if (!inactiveAssignments.isEmpty()) {
            // Removed judges without scoring are setup-only rows; delete them so the round
            // FK can be removed safely.
            roundJudgeRepository.deleteAll(inactiveAssignments);
        }
    }

}
