package com.fpt.swp.sealhackathonbe.award.service;

import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPatternResponse;
import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
import com.fpt.swp.sealhackathonbe.award.dto.RankingAwardCandidateResponse;

import java.util.List;
import java.util.UUID;

public interface AwardService {
    AwardResponse grantAward(AwardRequest request, UUID adminId);

    AwardResponse getAwardById(UUID awardId);

    List<AwardResponse> getAwardsByEvent(UUID eventId);

    List<HallOfFameResponse> getHallOfFameData();

    List<AwardPatternResponse> saveTop10Pattern(UUID categoryId, AwardPatternRequest request);

    List<AwardPatternResponse> getTop10Pattern(UUID categoryId);

    List<RankingAwardCandidateResponse> getTop10RankingByCategory(UUID categoryId, UUID roundId);

    List<AwardResponse> autoGrantTop10Awards(UUID categoryId, UUID roundId, UUID adminId);
}
