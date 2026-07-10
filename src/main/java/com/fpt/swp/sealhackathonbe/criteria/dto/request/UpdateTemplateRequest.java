package com.fpt.swp.sealhackathonbe.criteria.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateTemplateRequest {
    private String criterionName;
    private String description;
    private BigDecimal defaultWeight;
    private BigDecimal maxScore;
}
