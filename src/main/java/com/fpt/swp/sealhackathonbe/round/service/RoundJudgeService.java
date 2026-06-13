package com.fpt.swp.sealhackathonbe.round.service;

import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RoundJudgeService {
    List<RoundJudgeResponse> assignJudges(UUID roundId, AssignJudgesRequest request);
    List<JudgeResponse> getJudgesByRound(UUID roundId);
    void removeJudge(UUID roundJudgeId);
}
