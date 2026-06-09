package com.fpt.swp.sealhackathonbe.round.service;

import java.util.UUID;

public interface RoundService {
    void assignJudge(UUID roundId, UUID judgeUserId);
}
