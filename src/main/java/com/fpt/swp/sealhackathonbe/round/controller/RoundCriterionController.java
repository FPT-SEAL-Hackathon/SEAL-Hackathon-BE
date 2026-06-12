package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.CreateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.ImportCriteriaFromEventRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateImportedCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundCriterionResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundCriterionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/rounds/criteria")
@RequiredArgsConstructor
public class RoundCriterionController {

    private final RoundCriterionService roundCriterionService;

    @GetMapping("/{id}")
    public RoundCriterionResponse getById(@PathVariable UUID id) {
        return roundCriterionService.getById(id);
    }

    @GetMapping("/{roundId}")
    public List<RoundCriterionResponse> getByRound(@PathVariable UUID roundId) {
        return roundCriterionService.getByRound(roundId);
    }

    @PostMapping("/import/{roundId}")
    public List<RoundCriterionResponse> importCriteriaFromEvent(
            @PathVariable UUID roundId,
            @Valid @RequestBody ImportCriteriaFromEventRequest request) {
        return roundCriterionService.importCriteriaFromEvent(roundId, request);
    }

    @PostMapping("/{roundId}")
    public RoundCriterionResponse createSpecificCriterion(
            @PathVariable UUID roundId,
            @Valid @RequestBody CreateSpecificCriterionRequest request) {
        return roundCriterionService.createSpecificCriterion(roundId, request);
    }

    @PutMapping("/import/{id}")
    public RoundCriterionResponse updateImportedCriterion(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateImportedCriterionRequest request) {
        return roundCriterionService.updateImportedCriterion(id, request);
    }

    @PutMapping("/{id}")
    public RoundCriterionResponse updateSpecificCriterion(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSpecificCriterionRequest request
            ) {
        return roundCriterionService.updateSpecificCriterion(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        roundCriterionService.delete(id);
    }

}