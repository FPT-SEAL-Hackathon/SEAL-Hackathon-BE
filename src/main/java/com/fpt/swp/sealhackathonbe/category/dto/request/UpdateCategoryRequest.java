package com.fpt.swp.sealhackathonbe.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateCategoryRequest {
    @NotBlank(message = "Category name must not be empty")
    private String categoryName;
    private String description;
    private Integer sortOrder;
}
