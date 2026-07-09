package com.fpt.swp.sealhackathonbe.category.service;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.category.dto.request.AssignMentorsRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CategoryMentorService {
    List<CategoryMentorResponse> assignMentors(UUID categoryId, AssignMentorsRequest request);
    List<UserResponse> getAllMentors();
    List<CategoryMentorResponse> getMentorsByCategory(UUID categoryId);
    List<CategoryMentorResponse> getCategoryMentors(UUID categoryId);
}
