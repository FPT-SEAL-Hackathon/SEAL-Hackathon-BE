package com.fpt.swp.sealhackathonbe.round.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class UpdateImportedCriterionRequest {
    private BigDecimal weight;
    private BigDecimal maxScore;
    private Integer sortOrder;
}
