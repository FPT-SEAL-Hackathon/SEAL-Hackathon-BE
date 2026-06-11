package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HallOfFameResponse {
    private String eventName;
    private String categoryName;
    private String teamName;
    private String awardTierName; // VD: Giải Nhất, Giải Nhì
    private String awardTitle;    // VD: Quán quân Hackathon 2026
}