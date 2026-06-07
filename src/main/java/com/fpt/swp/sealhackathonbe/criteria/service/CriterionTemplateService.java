package com.fpt.swp.sealhackathonbe.criteria.service;

import java.util.List;
import java.util.UUID;

public interface CriterionTemplateService {
    void importTemplateToEvent(UUID eventId, List<UUID> templateIds);
}
