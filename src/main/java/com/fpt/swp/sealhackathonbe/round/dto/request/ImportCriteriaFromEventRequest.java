package com.fpt.swp.sealhackathonbe.round.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ImportCriteriaFromEventRequest {
    private List<UUID> eventCriterionIds;
}
