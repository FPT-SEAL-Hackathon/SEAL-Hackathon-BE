package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HallOfFameResponse {
    private String eventName;
    private String categoryName;
    private String teamName;
    private String awardTierName; // Example: First Prize, Second Prize
    private String awardTitle;    // Example: Hackathon 2026 Champion
}
