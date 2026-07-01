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
                .eventId(category.getEvent().getEventId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .build();
    }

    public CategoryMentorResponse categoryMentorResponse(CategoryMentor categoryMentor) {
        return CategoryMentorResponse.builder()
                .categoryMentorId(categoryMentor.getCategoryMentorId())
                .categoryId(categoryMentor.getCategory().getCategoryId())
                .mentorId(categoryMentor.getMentor().getUserId())
                .mentorName(categoryMentor.getMentor().getProfile().getFirstName() + " " + categoryMentor.getMentor().getProfile().getLastName())
                .mentorEmail(categoryMentor.getMentor().getEmail())
                .assignedAt(categoryMentor.getAssignedAt())
                .build();
    }
}
