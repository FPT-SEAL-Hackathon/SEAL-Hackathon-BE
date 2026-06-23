package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AwardResponse {
    private UUID id;
    private UUID eventId;
    private String eventName;
    private UUID categoryId;
    private String categoryName;
    private UUID teamId;
    private String teamName;
    private UUID awardTierId;
    private String awardTierName;
    private String awardTitle;
    private String description;
    private BigDecimal prizeValue;
    private String prizeCurrency;
    private Instant awardedAt;
    private String awardedByName; // Only returns the awarder's name for UI display
    private Boolean isPublished;
    private Instant publishedAt;
}
