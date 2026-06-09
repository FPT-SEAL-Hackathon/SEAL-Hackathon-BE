package com.fpt.swp.sealhackathonbe.category.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCategoryRequest {
    private UUID eventId;
    private String categoryName;
    private String description;
    private Integer sortOrder;
}
