package com.fpt.swp.sealhackathonbe.criteria.service.impl;

import com.fpt.swp.sealhackathonbe.criteria.dto.request.ImportCriteriaToEventRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.EventCriterionResponse;
import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import com.fpt.swp.sealhackathonbe.criteria.entity.EventCriteria;
import com.fpt.swp.sealhackathonbe.criteria.repository.CriterionTemplateRepository;
import com.fpt.swp.sealhackathonbe.criteria.repository.EventCriterionRepository;
import com.fpt.swp.sealhackathonbe.criteria.service.EventCriterionService;
import com.fpt.swp.sealhackathonbe.criteria.service.mapper.Mapper;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventCriterionServiceImpl implements EventCriterionService {

    private final EventRepository eventRepository;
    private final EventCriterionRepository eventCriterionRepository;
    private final CriterionTemplateRepository templateRepository;
    private final Mapper mapper;

    @Override
    public List<EventCriterionResponse> importCriteriaToEvent(UUID eventId, ImportCriteriaToEventRequest request) {
        //Get Event
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        //Get list of templates to import
        List<CriterionTemplate> templates = templateRepository.findAllById(request.getTemplateIds());
        if (templates.isEmpty()) {
            throw new IllegalArgumentException("No valid criterion templates found for the provided IDs");
        }
        //Calculate sort order
        int count = eventCriterionRepository.countByEventEventId(eventId);

        List<EventCriteria> eventCriteria = new ArrayList<>();
        for (CriterionTemplate template : templates) {
            count++;
            EventCriteria eventCriterion = EventCriteria.builder()
                    .eventCriterionId(UUID.randomUUID())
                    .event(event)
                    .criterionName(template.getCriterionName())
                    .description(template.getDescription())
                    .weight(template.getDefaultWeight())
                    .maxScore(template.getMaxScore())
                    .sortOrder(count)
                    .templateId(template.getTemplateId())
                    .isActive(true)
                    .build();
            eventCriteria.add(eventCriterion);
        }

        eventCriteria = eventCriterionRepository.saveAll(eventCriteria);

        return eventCriteria.stream()
                .map(mapper::toEventCriterionResponse)
                .toList();
    }
}
