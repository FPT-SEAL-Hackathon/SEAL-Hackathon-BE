package com.fpt.swp.sealhackathonbe.category.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateCategoryRequest {
    private String categoryName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
