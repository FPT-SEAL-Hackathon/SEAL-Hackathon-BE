package com.fpt.swp.sealhackathonbe.round.service;

import com.fpt.swp.sealhackathonbe.round.dto.request.CreateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.ImportCriteriaFromEventRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateImportedCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundCriterionResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RoundCriterionService {
    List<RoundCriterionResponse> importCriteriaFromEvent(UUID roundId, ImportCriteriaFromEventRequest request);
    RoundCriterionResponse updateImportedCriterion(UUID roundCriterionId, UpdateImportedCriterionRequest request);
    RoundCriterionResponse createSpecificCriterion(UUID roundId, CreateSpecificCriterionRequest request);
    RoundCriterionResponse updateSpecificCriterion(UUID roundCriterionId, UpdateSpecificCriterionRequest request);
    void delete(UUID roundCriterionId);
    RoundCriterionResponse getById(UUID roundCriterionId);
    List<RoundCriterionResponse> getByRound(UUID roundId);
}
