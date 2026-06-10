package com.fpt.swp.sealhackathonbe.criteria.dto.response;

import lombok.*;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriterionTemplateResponse {
    private UUID templateId;
    private String criterionName;
    private String description;
    private BigDecimal defaultWeight;
    private BigDecimal maxScore;
    private Boolean isActive;
    private UUID createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
