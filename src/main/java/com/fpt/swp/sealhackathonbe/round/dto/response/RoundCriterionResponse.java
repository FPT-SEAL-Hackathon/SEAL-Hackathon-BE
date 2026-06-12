package com.fpt.swp.sealhackathonbe.round.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RoundCriterionResponse {
    private UUID roundCriterionId;
    private UUID roundId;
    private UUID eventCriterionId;
    private String criterionName;
    private String description;
    private BigDecimal weight;
    private BigDecimal maxScore;
    private Integer sortOrder;
}
