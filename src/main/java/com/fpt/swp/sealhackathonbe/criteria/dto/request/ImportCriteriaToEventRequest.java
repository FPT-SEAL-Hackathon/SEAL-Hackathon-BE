package com.fpt.swp.sealhackathonbe.criteria.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ImportCriteriaToEventRequest {
    private List<UUID> templateIds;

}
