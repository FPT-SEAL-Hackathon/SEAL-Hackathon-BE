package com.fpt.swp.sealhackathonbe.round.service.mapper;

import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundCriterionResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.user.entity.User;
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
                .roundStatusId(round.getRoundStatus() != null ? round.getRoundStatus().getStatusId() : null)
                .roundStatusName(round.getRoundStatus() != null ? round.getRoundStatus().getStatusName() : null)
                .submissionDeadline(round.getSubmissionDeadline())
                .judgingDeadline(round.getJudgingDeadline())
                .startDate(round.getStartDate())
                .endDate(round.getEndDate())
                .appealStartTime(round.getAppealStartTime())
                .appealEndTime(round.getAppealEndTime())
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

    public RoundJudgeResponse toRoundJudgeResponse(RoundJudge roundJudge) {
        if (roundJudge == null) return null;
        var judge = roundJudge.getJudge();
        var round = roundJudge.getRound();
        var assignedBy = roundJudge.getAssignedBy();

        return RoundJudgeResponse.builder()
                .roundJudgeId(roundJudge.getRoundJudgeId())
                .roundId(round != null ? round.getRoundId() : null)
                .judgeId(judge != null ? judge.getUserId() : null)
                .fullName(judge != null ? judge.getFullName() : null)
                .email(judge != null ? judge.getEmail() : null)
                .phone(judge != null ? judge.getPhone() : null)
                .assignedAt(roundJudge.getAssignedAt())
                .assignedById(assignedBy != null ? assignedBy.getUserId() : null)
                .build();
    }

    public JudgeResponse toJudgeResponse(User judge) {
        return JudgeResponse.builder()
                .judgeId(judge.getUserId())
                .fullName(judge.getFullName())
                .email(judge.getEmail())
                .phone(judge.getPhone())
                .role(toApiName(getRoleName(judge)))
                .roleName(getRoleName(judge))
                .build();
    }

    private String getRoleName(User user) {
        return user.getUserType() != null ? user.getUserType().getTypeName() : null;
    }

    private String toApiName(String value) {
        return value == null ? null : value.trim().replace(' ', '_').toUpperCase();
    }
}
