package com.fpt.swp.sealhackathonbe.category.mapper;

import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .build();
    }

    public CategoryMentorResponse categoryMentorResponse(CategoryMentor categoryMentor) {
        return CategoryMentorResponse.builder()
                .categoryMentorId(categoryMentor.getCategoryMentorId())
                .categoryId(categoryMentor.getCategory().getCategoryId())
                .mentorId(categoryMentor.getMentor().getUserId())
                .assignedAt(categoryMentor.getAssignedAt())
                .build();
    }
}
