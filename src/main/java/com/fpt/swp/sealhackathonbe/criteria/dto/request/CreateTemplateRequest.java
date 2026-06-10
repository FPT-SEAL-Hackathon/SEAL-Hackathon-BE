package com.fpt.swp.sealhackathonbe.criteria.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateTemplateRequest {
    private String criterionName;
    private String description;
    private BigDecimal defaultWeight;
    private BigDecimal maxScore;
    private UUID createsByUserId;
}
