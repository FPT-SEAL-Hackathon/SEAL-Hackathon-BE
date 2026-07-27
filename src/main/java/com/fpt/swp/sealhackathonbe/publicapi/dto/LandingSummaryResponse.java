package com.fpt.swp.sealhackathonbe.publicapi.dto;

import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
import com.fpt.swp.sealhackathonbe.award.dto.SystemAwardPrizeTotalResponse;
import com.fpt.swp.sealhackathonbe.event.dto.EventResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingSummaryResponse {
    private List<EventResponse> events;
    private long totalTeams;
    private SystemAwardPrizeTotalResponse totalPrize;
    private List<HallOfFameResponse> hallOfFame;
}
