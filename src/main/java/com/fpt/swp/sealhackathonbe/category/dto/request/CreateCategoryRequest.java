package com.fpt.swp.sealhackathonbe.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCategoryRequest {
    @NotBlank(message = "Category name must not be empty")
    private String categoryName;
    private String description;

    private Integer sortOrder;
}
