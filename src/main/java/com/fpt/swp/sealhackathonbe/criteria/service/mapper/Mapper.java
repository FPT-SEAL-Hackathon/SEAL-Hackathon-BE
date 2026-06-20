package com.fpt.swp.sealhackathonbe.criteria.service.mapper;

import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.EventCriterionResponse;
import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import com.fpt.swp.sealhackathonbe.criteria.entity.EventCriteria;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Mapper {
    public CriterionTemplateResponse toTemplateResponse(CriterionTemplate template) {
        return CriterionTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .criterionName(template.getCriterionName())
                .description(template.getDescription())
                .defaultWeight(template.getDefaultWeight())
                .maxScore(template.getMaxScore())
                .isActive(template.getIsActive())
                .createdById(template.getCreatedBy().getUserId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public EventCriterionResponse toEventCriterionResponse(EventCriteria eventCriteria) {
        return EventCriterionResponse.builder()
                .eventCriterionId(eventCriteria.getEventCriterionId())
                .templateId(eventCriteria.getTemplateId())
                .eventId(eventCriteria.getEvent().getEventId())
                .criterionName(eventCriteria.getCriterionName())
                .description(eventCriteria.getDescription())
                .weight(eventCriteria.getWeight())
                .maxScore(eventCriteria.getMaxScore())
                .sortOrder(eventCriteria.getSortOrder())
                .isActive(eventCriteria.getIsActive())
                .build();
    }
}
