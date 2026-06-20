package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class AwardPatternResponse {
    private UUID id;
    private UUID eventId;
    private String eventName;
    private UUID categoryId;
    private String categoryName;
    private Integer rankPosition;
    private UUID awardTierId;
    private String awardTierName;
    private String awardTitle;
    private String description;
    private BigDecimal prizeValue;
    private String prizeCurrency;
    private Boolean isActive;
}
