package com.fpt.swp.sealhackathonbe.round.service;

import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RoundService {
    RoundResponse create(CreateRoundRequest request);
    RoundResponse getById(UUID roundId);
    List<RoundResponse> getByCategory(UUID categoryId);
    RoundResponse update(UUID roundId, UpdateRoundRequest request);
    void delete(UUID roundId);
    RoundResponse getFinalRound(UUID categoryId);
    Integer getAdvancementTopN(UUID roundId);
}
