package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class EventPrizeTotalResponse {
    private UUID eventId;
    private String eventName;
    private List<AwardPrizeTotalResponse> totalPrizes;
}
