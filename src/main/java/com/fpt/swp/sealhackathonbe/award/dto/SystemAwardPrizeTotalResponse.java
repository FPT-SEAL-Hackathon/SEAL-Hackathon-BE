package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SystemAwardPrizeTotalResponse {
    private List<AwardPrizeTotalResponse> totalPrizes;
    private List<EventPrizeTotalResponse> events;
}
