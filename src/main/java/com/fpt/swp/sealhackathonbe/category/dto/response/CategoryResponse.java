package com.fpt.swp.sealhackathonbe.category.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private UUID categoryId;
    private UUID eventId;
    private String categoryName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
