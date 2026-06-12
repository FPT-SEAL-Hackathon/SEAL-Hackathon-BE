package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.criteria.entity.EventCriteria;
import com.fpt.swp.sealhackathonbe.criteria.repository.EventCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.dto.request.CreateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.ImportCriteriaFromEventRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateImportedCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateSpecificCriterionRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundCriterionResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundCriterionService;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundCriterionServiceImpl implements RoundCriterionService {
    private final RoundRepository roundRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    private final EventCriterionRepository eventCriterionRepository;
    private final RoundMapper roundMapper;

    @Override
    public List<RoundCriterionResponse> importCriteriaFromEvent(UUID roundId, ImportCriteriaFromEventRequest request) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
        List<EventCriteria> eventCriteria = eventCriterionRepository.findAllById(request.getEventCriterionIds());

        if (eventCriteria.isEmpty()) {
            throw new RuntimeException("Event criteria not found");
        }

        List<RoundCriterion> roundCriteria = eventCriteria
                .stream()
                .map(eventCriterion -> RoundCriterion.builder()
                        .round(round)
                        .eventCriterionId(eventCriterion.getEventCriterionId())
                        .criterionName(eventCriterion.getCriterionName())
                        .description(eventCriterion.getDescription())
                        .sortOrder(eventCriterion.getSortOrder())
                        .weight(eventCriterion.getWeight())
                        .maxScore(eventCriterion.getMaxScore())
                        .build()
                )
                .toList();

        roundCriteria = roundCriterionRepository.saveAll(roundCriteria);
        return roundCriteria
                .stream()
                .map(roundMapper::toRoundCriterionResponse)
                .toList();
    }

    @Override
    public RoundCriterionResponse updateImportedCriterion(UUID roundCriterionId, UpdateImportedCriterionRequest request) {
        RoundCriterion roundCriterion = roundCriterionRepository
                .findById(roundCriterionId)
                .orElseThrow(() -> new RuntimeException("Round criterion not found"));

        roundCriterion.setWeight(request.getWeight());
        roundCriterion.setMaxScore(request.getMaxScore());
        roundCriterion.setSortOrder(request.getSortOrder());

        return roundMapper.toRoundCriterionResponse(roundCriterionRepository.save(roundCriterion));
    }

    @Override
    public RoundCriterionResponse createSpecificCriterion(UUID roundId ,CreateSpecificCriterionRequest request) {
        RoundCriterion roundCriterion = RoundCriterion.builder()
                .roundCriterionId(UUID.randomUUID())
                .eventCriterionId(null)
                .criterionName(request.getCriterionName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .maxScore(request.getMaxScore())
                .sortOrder(request.getSortOrder())
                .build();
        return roundMapper.toRoundCriterionResponse(roundCriterionRepository.save(roundCriterion));
    }

    @Override
    public RoundCriterionResponse updateSpecificCriterion(UUID roundCriterionId, UpdateSpecificCriterionRequest request) {
        RoundCriterion roundCriterion = roundCriterionRepository
                .findById(roundCriterionId)
                .orElseThrow(() -> new RuntimeException("Round criterion not found"));

        roundCriterion.setCriterionName(request.getCriterionName());
        roundCriterion.setDescription(request.getDescription());
        roundCriterion.setWeight(request.getWeight());
        roundCriterion.setMaxScore(request.getMaxScore());
        roundCriterion.setSortOrder(request.getSortOrder());

        return roundMapper.toRoundCriterionResponse(roundCriterionRepository.save(roundCriterion));
    }

    @Override
    public void delete(UUID roundCriterionId) {
        RoundCriterion roundCriterion = roundCriterionRepository
                .findById(roundCriterionId)
                .orElseThrow(() -> new RuntimeException("Round criterion not found"));

        if (!"Upcoming".equalsIgnoreCase(roundCriterion.getRound().getRoundStatus().getStatusName())) {
            throw new IllegalArgumentException("Only delete criterion of round at status 'Upcoming'");
        }

        //Add check isScored later

        roundCriterionRepository.delete(roundCriterion);
    }

    @Override
    public RoundCriterionResponse getById(UUID roundCriterionId) {
        RoundCriterion roundCriterion = roundCriterionRepository
                .findById(roundCriterionId)
                .orElseThrow(() -> new RuntimeException("Round criterion not found"));
        return roundMapper.toRoundCriterionResponse(roundCriterion);
    }

    @Override
    public List<RoundCriterionResponse> getByRound(UUID roundId) {
        return roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)
                .stream()
                .map(roundMapper::toRoundCriterionResponse)
                .toList();
    }

}
