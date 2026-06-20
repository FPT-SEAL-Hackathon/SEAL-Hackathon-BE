package com.fpt.swp.sealhackathonbe.criteria.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCriterionResponse {
    private UUID eventCriterionId;
    private UUID templateId;
    private UUID eventId;
    private String criterionName;
    private String description;
    private BigDecimal weight;
    private BigDecimal maxScore;
    private Integer sortOrder;
    private Boolean isActive;
}
