package com.fpt.swp.sealhackathonbe.consultation.dto;

import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MentorProfileResponse {
    private UUID mentorId;
    private String fullName;
    private String email;
    private String department;
    private String specialization;
    private String bio;
    private UUID categoryId;
    private String categoryName;

    public static MentorProfileResponse from(CategoryMentor cm) {
        return MentorProfileResponse.builder()
                .mentorId(cm.getMentor().getUserId())
                .fullName(cm.getMentor().getFullName())
                .email(cm.getMentor().getEmail())
                // Assuming department, specialization, bio might be on User or just leave null if not available
                .department(cm.getMentor().getUniversityName()) // Using university name as fallback for department
                .categoryId(cm.getCategory().getCategoryId())
                .categoryName(cm.getCategory().getCategoryName())
                .build();
    }
}
