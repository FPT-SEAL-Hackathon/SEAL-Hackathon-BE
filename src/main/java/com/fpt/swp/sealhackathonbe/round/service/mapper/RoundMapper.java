package com.fpt.swp.sealhackathonbe.round.service.mapper;

import com.fpt.swp.sealhackathonbe.round.dto.response.RoundCriterionResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import org.springframework.stereotype.Component;

@Component
public class RoundMapper {
    public RoundResponse toRoundResponse(Round round) {
        return RoundResponse.builder()
                .roundId(round.getRoundId())
                .categoryId(round.getCategory().getCategoryId())
                .roundName(round.getRoundName())
                .description(round.getDescription())
                .roundOrder(round.getRoundOrder())
                .roundStatusId(round.getRoundStatus().getStatusId())
                .submissionDeadline(round.getSubmissionDeadline())
                .judgingDeadline(round.getJudgingDeadline())
                .startDate(round.getStartDate())
                .endDate(round.getEndDate())
                .advancementTopN(round.getAdvancementTopN())
                .isCalibrationRound(round.getIsCalibrationRound())
                .build();
    }

    public RoundCriterionResponse toRoundCriterionResponse(RoundCriterion roundCriterion) {
        return RoundCriterionResponse.builder()
                .roundCriterionId(roundCriterion.getRoundCriterionId())
                .roundId(roundCriterion.getRound().getRoundId())
                .eventCriterionId(roundCriterion.getEventCriterionId())
                .criterionName(roundCriterion.getCriterionName())
                .description(roundCriterion.getDescription())
                .weight(roundCriterion.getWeight())
                .maxScore(roundCriterion.getMaxScore())
                .sortOrder(roundCriterion.getSortOrder())
                .build();
    }
}
