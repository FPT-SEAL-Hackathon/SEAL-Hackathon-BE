package com.fpt.swp.sealhackathonbe.criteria.service.impl;

import com.fpt.swp.sealhackathonbe.criteria.service.CriterionTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CriterionTemplateServiceImpl implements CriterionTemplateService {
    @Override
    public void importTemplateToEvent(UUID eventId, List<UUID> templateIds) {
        // Implementation logic to import criterion templates to the specified event
        // This may involve fetching the templates by their IDs, validating them,
        // and then associating them with the event in the database.
    }
}
