package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RankingAwardCandidateResponse {
    private UUID rankingId;
    private UUID roundId;
    private String roundName;
    private UUID categoryId;
    private String categoryName;
    private UUID teamId;
    private String teamName;
    private BigDecimal totalScore;
    private BigDecimal averageScore;
    private Integer rankPosition;
    private Boolean isAdvanced;
}
