package com.fpt.swp.sealhackathonbe.round.dto.request;

import com.fpt.swp.sealhackathonbe.round.entity.Round;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSpecificCriterionRequest {
    private String criterionName;
    private String description;
    private BigDecimal weight;
    private BigDecimal maxScore;
    private Integer sortOrder;
}
