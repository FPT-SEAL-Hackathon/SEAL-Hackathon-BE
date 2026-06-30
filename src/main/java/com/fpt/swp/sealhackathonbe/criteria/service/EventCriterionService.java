package com.fpt.swp.sealhackathonbe.criteria.service;

import com.fpt.swp.sealhackathonbe.criteria.dto.request.ImportCriteriaToEventRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.EventCriterionResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface EventCriterionService {
    List<EventCriterionResponse> importCriteriaToEvent(UUID eventId, ImportCriteriaToEventRequest request);
    List<EventCriterionResponse> getCriteriaByEvent(UUID eventId);

}
