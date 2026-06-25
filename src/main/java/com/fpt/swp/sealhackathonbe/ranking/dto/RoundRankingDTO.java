package com.fpt.swp.sealhackathonbe.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundRankingDTO {
    private UUID id;
    private UUID roundId;
    private UUID categoryId;
    private UUID teamId;
    private UUID submissionId;
    private BigDecimal totalScore;
    private BigDecimal averageScore;
    private Integer rankPosition;
    private Boolean isAdvanced;
    private LocalDateTime computedAt;
    private Boolean isPublished;
}
