package com.fpt.swp.sealhackathonbe.consultation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AssignedCategoryResponse {
    private UUID categoryId;
    private String categoryName;
    private UUID eventId;
    private String eventName;
    private int numberOfTeams;
    private int numberOfOpenRequests;
}
