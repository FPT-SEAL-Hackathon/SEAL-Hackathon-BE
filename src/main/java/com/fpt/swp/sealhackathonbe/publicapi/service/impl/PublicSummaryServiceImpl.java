package com.fpt.swp.sealhackathonbe.publicapi.service.impl;

import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import com.fpt.swp.sealhackathonbe.publicapi.dto.LandingSummaryResponse;
import com.fpt.swp.sealhackathonbe.publicapi.service.PublicSummaryService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicSummaryServiceImpl implements PublicSummaryService {

    private final EventService eventService;
    private final TeamService teamService;
    private final AwardService awardService;

    @Override
    public LandingSummaryResponse getLandingSummary() {
        return LandingSummaryResponse.builder()
                .events(eventService.getPublicEvents())
                .totalTeams(teamService.countAllTeams())
                .totalPrize(awardService.getSystemPrizeTotal())
                .hallOfFame(awardService.getHallOfFameData())
                .build();
    }
}
