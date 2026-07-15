package com.fpt.swp.sealhackathonbe.criteria.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateEventCriterionRequest {
    private BigDecimal weight;
    private BigDecimal maxScore;
}
