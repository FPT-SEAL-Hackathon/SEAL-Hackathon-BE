package com.fpt.swp.sealhackathonbe.category.mapper;

import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.dto.response.MentorResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;
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

    public CategoryMentorResponse toCategoryMentorResponse(CategoryMentor categoryMentor) {
        return CategoryMentorResponse.builder()
                .categoryMentorId(categoryMentor.getCategoryMentorId())
                .categoryId(categoryMentor.getCategory().getCategoryId())
                .mentorId(categoryMentor.getMentor().getUserId())
                .fullName(categoryMentor.getMentor().getFullName())
                .email(categoryMentor.getMentor().getEmail())
                .phone(categoryMentor.getMentor().getPhone())
                .mentorName(categoryMentor.getMentor().getFullName())
                .mentorEmail(categoryMentor.getMentor().getEmail())
                .assignedAt(categoryMentor.getAssignedAt())
                .build();
    }

    public MentorResponse toMentorResponse(User mentor) {
        return MentorResponse.builder()
                .mentorId(mentor.getUserId())
                .fullName(mentor.getFullName())
                .email(mentor.getEmail())
                .phone(mentor.getPhone())
                .build();
    }
}
